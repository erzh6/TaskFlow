package org.megalab.taskflow.dto;

import lombok.Getter;
import lombok.Setter;
import org.megalab.taskflow.model.TaskPriority;
import org.megalab.taskflow.model.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDate deadline;
    private String assignee;
    private boolean overdue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}