package org.megalab.taskflow.controller;

import jakarta.validation.Valid;
import org.megalab.taskflow.dto.*;
import org.megalab.taskflow.model.TaskPriority;
import org.megalab.taskflow.model.TaskStatus;
import org.megalab.taskflow.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAll(@RequestParam(required = false) TaskStatus status,
                                                     @RequestParam(required = false) TaskPriority priority,
                                                     @RequestParam(required = false) String assignee,
                                                     @RequestParam(required = false) String search) {
        return ResponseEntity.ok(taskService.getAll(status, priority, assignee, search));
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<TaskResponse>> getOverdue() {
        return ResponseEntity.ok(taskService.getOverdue());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getById(id));
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request) {
        TaskResponse created = taskService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/tasks/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> update(@PathVariable Long id, @Valid @RequestBody TaskUpdateRequest request) {
        return ResponseEntity.ok(taskService.update(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TaskResponse> patch(@PathVariable Long id, @Valid @RequestBody TaskPatch request) {
        return ResponseEntity.ok(taskService.patch(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/stats")
   ResponseEntity<StatsResponse> getStats() {
        return ResponseEntity.ok(taskService.getStats());
    }
}
