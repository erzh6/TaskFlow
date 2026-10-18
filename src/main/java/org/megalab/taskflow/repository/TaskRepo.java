package org.megalab.taskflow.repository;

import org.megalab.taskflow.model.Task;
import org.megalab.taskflow.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
@Repository
public interface TaskRepo extends JpaRepository<Task, Long> {

    List<Task> findAllByOrderByCreatedAtDesc();

    long countByAssigneeAndStatusIn(String assignee, List<TaskStatus> statuses);

    List<Task> findByStatusInAndDeadlineBeforeOrderByDeadlineAsc(List<TaskStatus> statuses, LocalDate date);

    long countByStatus(TaskStatus status);
}
