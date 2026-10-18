package org.megalab.taskflow.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.megalab.taskflow.model.TaskPriority;
import org.megalab.taskflow.model.TaskStatus;

import java.time.LocalDate;

@Getter
@Setter

public class TaskUpdateRequest {

    @Size(min = 3, max = 100, message = "Title must be 3-100 characters")
    private String title;

    @Size(max = 500, message = "Description max 500 characters")
    private String description;

    private LocalDate deadline;

    @Size(max = 100, message = "Assignee max 100 characters")
    private String assignee;

    @NotNull(message = "Status is required")
    private TaskStatus status;

    @NotNull(message = "Priority is required")
    private TaskPriority priority;
}
