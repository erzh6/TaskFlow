package org.megalab.taskflow.service;

import org.megalab.taskflow.dto.*;
import org.megalab.taskflow.model.TaskPriority;
import org.megalab.taskflow.model.TaskStatus;
import org.springframework.stereotype.Service;

import java.util.List;

public interface TaskService {

    List<TaskResponse> getAll(TaskStatus status, TaskPriority priority, String assignee, String search);

    TaskResponse getById(Long id);

    void delete(Long id);

    TaskResponse patch(Long id, TaskPatch request);

    TaskResponse update(Long id, TaskUpdateRequest request);

    TaskResponse create(TaskRequest request);

    List<TaskResponse> getOverdue();

    StatsResponse getStats();
}
