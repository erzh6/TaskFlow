package org.megalab.taskflow.mapper;

import org.megalab.taskflow.dto.TaskRequest;
import org.megalab.taskflow.dto.TaskResponse;
import org.megalab.taskflow.model.Task;
import org.megalab.taskflow.model.TaskPriority;
import org.megalab.taskflow.model.TaskStatus;

import java.time.LocalDate;

public class TaskMapper {

    public static Task toTask(TaskRequest request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setDeadline(request.getDeadline());
        task.setAssignee(request.getAssignee());

        if (request.getPriority() == null) {
            task.setPriority(TaskPriority.MEDIUM);
            if (request.getPriority() != null)
                task.setPriority(request.getPriority());
        }
        return task;
    }

    public static TaskResponse toTaskResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatus());
        response.setPriority(task.getPriority());
        response.setDeadline(task.getDeadline());
        response.setAssignee(task.getAssignee());
        response.setOverdue(isOverdue(task));
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());
        return response;
    }

    private static boolean isOverdue(Task task) {
        boolean active = task.getStatus() == TaskStatus.NEW || task.getStatus() == TaskStatus.IN_PROGRESS;
        return active && task.getDeadline() != null && task.getDeadline().isBefore(LocalDate.now());
    }
}