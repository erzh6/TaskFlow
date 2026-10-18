package org.megalab.taskflow.service.impl;

import org.megalab.taskflow.dto.*;
import org.megalab.taskflow.exception.BadRequestException;
import org.megalab.taskflow.exception.ConflictException;
import org.megalab.taskflow.exception.NoFoundException;
import org.megalab.taskflow.mapper.TaskMapper;
import org.megalab.taskflow.model.Task;
import org.megalab.taskflow.model.TaskPriority;
import org.megalab.taskflow.model.TaskStatus;
import org.megalab.taskflow.repository.TaskRepo;
import org.megalab.taskflow.service.TaskService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    private static final List<TaskStatus> ACTIVE_STATUSES = List.of(TaskStatus.NEW, TaskStatus.IN_PROGRESS);

    private static final int MAX_ACTIVE_TASKS = 10;

    private final TaskRepo taskRepo;

    private static final Map<TaskStatus, Set<TaskStatus>> ALLOWED_TRANSITIONS = Map.of(
            TaskStatus.NEW, Set.of(TaskStatus.IN_PROGRESS, TaskStatus.CANCELLED),
            TaskStatus.IN_PROGRESS, Set.of(TaskStatus.DONE, TaskStatus.CANCELLED),
            TaskStatus.DONE, Set.of(),
            TaskStatus.CANCELLED, Set.of());

    //private Set<TaskChange> allowedTaskChanges;

    public TaskServiceImpl(TaskRepo taskRepo) {

        this.taskRepo = taskRepo;
       // allowedTaskChanges.add(new TaskChange(TaskStatus.NEW, TaskStatus.CANCELLED));
    }

    @Override
//    public List<TaskResponse> getAll(TaskStatus status, TaskPriority priority, String assignee, String search) {
//        List<Task> tasks = taskRepo.findAllByOrderByCreatedAtDesc();
//        List<TaskResponse> result = new ArrayList<>();
//
//сделать в stream api

        public List<TaskResponse> getAll(TaskStatus status, TaskPriority priority, String assignee, String search) {
            return taskRepo.findAllByOrderByCreatedAtDesc().stream()
                    .filter(task -> status == null || task.getStatus() == status)
                    .filter(task -> priority == null || task.getPriority() == priority)
                    .filter(task -> assignee == null || assignee.equals(task.getAssignee()))
                    .filter(task -> search == null || (task.getTitle() != null && task.getTitle().toLowerCase().contains(search.toLowerCase())))
                    .map(TaskMapper::toTaskResponse)
                    .toList();
        }
//        for (Task task : tasks) {
//            if (status != null && task.getStatus() != status) {
//                continue;
//            }
//            if (priority != null && task.getPriority() != priority) {
//                continue;
//            }
//            if (assignee != null && !assignee.equals(task.getAssignee())) {
//                continue;
//            }
//            if (search != null && !task.getTitle().toLowerCase().contains(search.toLowerCase())) {
//                continue;
//            }
//            result.add(TaskMapper.toTaskResponse(task));
//        }
//        return result;
//    }

    @Override
    public TaskResponse getById(Long id) {
        Task task = findTask(id);
        return TaskMapper.toTaskResponse(task);
    }

    @Override
    public TaskResponse create(TaskRequest request) {

        if (request.getAssignee() != null) {
            checkLimit(request.getAssignee());
        }

        Task task = TaskMapper.toTask(request);



        Task saved = taskRepo.save(task);
        return TaskMapper.toTaskResponse(saved);
    }


    @Override
    public TaskResponse update(Long id, TaskUpdateRequest request) {
        Task task = findTask(id);

        checkNotClosed(task);
        checkStatusChange(task.getStatus(), request.getStatus());
        checkDoneHasAssignee(request.getStatus(), request.getAssignee());
        if (request.getAssignee() != null && !request.getAssignee().equals(task.getAssignee())) {
            checkLimit(request.getAssignee());
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setDeadline(request.getDeadline());
        task.setAssignee(request.getAssignee());
        task.setUpdatedAt(LocalDateTime.now());

        Task saved = taskRepo.save(task);
        return TaskMapper.toTaskResponse(saved);
    }

    @Override
    public TaskResponse patch(Long id, TaskPatch request) {
        Task task = findTask(id);
        TaskStatus newStatus = task.getStatus();
        if (request.getStatus() != null) {
            newStatus = request.getStatus();
        }
        String newAssignee = task.getAssignee();
        if (request.getAssignee() != null) {
            newAssignee = request.getAssignee();
        }

        checkNotClosed(task);
        checkStatusChange(task.getStatus(), newStatus);
        checkDoneHasAssignee(newStatus, newAssignee);
        if (newAssignee != null && !newAssignee.equals(task.getAssignee())) {
            checkLimit(newAssignee);
        }

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getDeadline() != null) {
            task.setDeadline(request.getDeadline());
        }
        task.setStatus(newStatus);
        task.setAssignee(newAssignee);
        task.setUpdatedAt(LocalDateTime.now());

        Task saved = taskRepo.save(task);
        return TaskMapper.toTaskResponse(saved);
    }

    @Override
    public void delete(Long id) {
        Task task = findTask(id);
        taskRepo.delete(task);
    }

    @Override
    public List<TaskResponse> getOverdue() {
        List<Task> tasks = taskRepo.findByStatusInAndDeadlineBeforeOrderByDeadlineAsc(ACTIVE_STATUSES, LocalDate.now());
        List<TaskResponse> result = new ArrayList<>();
        for (Task task : tasks) {
            result.add(TaskMapper.toTaskResponse(task));
        }
        return result;
    }

    @Override
    public StatsResponse getStats() {
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (TaskStatus status : TaskStatus.values()) {
            byStatus.put(status.name(), taskRepo.countByStatus(status));
        }
        long total = taskRepo.count();
        long overdue = taskRepo.findByStatusInAndDeadlineBeforeOrderByDeadlineAsc(ACTIVE_STATUSES, LocalDate.now()).size();
        return new StatsResponse(total, byStatus, overdue);
    }

    private Task findTask(Long id) {
        return taskRepo.findById(id)
                .orElseThrow(() -> new NoFoundException("Task " + id + " not found"));
    }

    private void checkNotClosed(Task task) {
        if (task.getStatus() == TaskStatus.DONE || task.getStatus() == TaskStatus.CANCELLED) {
            throw new ConflictException("Task with status " + task.getStatus() + " can not be edited");
        }
    }


    private void checkStatusChange(TaskStatus oldStatus, TaskStatus newStatus) {
        if (oldStatus == newStatus) {
            return;
        }
        if (!ALLOWED_TRANSITIONS.get(oldStatus).contains(newStatus)) {
            throw new ConflictException("Can't move status from " + oldStatus + " to " + newStatus);
        }
    }

//    private void checkStatusChange(TaskStatus oldStatus, TaskStatus newStatus) {
//
//        if (oldStatus == newStatus) {
//            return;
//        }
//        boolean allowed = false;
//        if (oldStatus == TaskStatus.NEW) {
//            allowed = newStatus == TaskStatus.IN_PROGRESS || newStatus == TaskStatus.CANCELLED;
//        }
//        if (oldStatus == TaskStatus.IN_PROGRESS) {
//            allowed = newStatus == TaskStatus.DONE || newStatus == TaskStatus.CANCELLED;
//        }
//        if (!allowed) {
//            throw new ConflictException("Can't move status from " + oldStatus + " to " + newStatus);
//        }
//    }
//


    private record TaskChange(TaskStatus from, TaskStatus to){}

    private void checkDoneHasAssignee(TaskStatus status, String assignee) {
        if (status == TaskStatus.DONE && (assignee == null || assignee.isBlank())) {
            throw new BadRequestException("Can't make status DONE without Assignee");
        }
    }

    private void checkLimit(String assignee) {
        long count = taskRepo.countByAssigneeAndStatusIn(assignee, ACTIVE_STATUSES);
        if (count >= MAX_ACTIVE_TASKS) {
            throw new ConflictException("Assignee " + assignee + " already has " + count + " active tasks");
        }
    }
}