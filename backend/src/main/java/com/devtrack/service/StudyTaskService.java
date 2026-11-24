package com.devtrack.service;

import com.devtrack.dto.RequestDtos.TaskRequest;
import com.devtrack.entity.Enums.TaskStatus;
import com.devtrack.entity.StudySession;
import com.devtrack.entity.StudyTask;
import com.devtrack.exception.ResourceNotFoundException;
import com.devtrack.repository.StudySessionRepository;
import com.devtrack.repository.StudyTaskRepository;
import com.devtrack.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudyTaskService {
    private final StudyTaskRepository tasks;
    private final StudySessionRepository sessions;
    private final UserRepository users;

    public StudyTaskService(StudyTaskRepository tasks, StudySessionRepository sessions, UserRepository users) {
        this.tasks = tasks;
        this.sessions = sessions;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<StudyTask> list(Long userId, TaskStatus status, LocalDate from, LocalDate to) {
        return tasks.findByUserIdOrderByScheduledDateDescIdDesc(userId).stream()
                .filter(t -> status == null || t.getStatus() == status)
                .filter(t -> from == null || !t.getScheduledDate().isBefore(from))
                .filter(t -> to == null || !t.getScheduledDate().isAfter(to))
                .toList();
    }

    @Transactional
    public StudyTask create(Long userId, TaskRequest req) {
        StudyTask task = new StudyTask();
        task.setUser(users.getReferenceById(userId));
        apply(task, req);
        return tasks.save(task);
    }

    @Transactional
    public StudyTask update(Long userId, Long id, TaskRequest req) {
        StudyTask task = find(userId, id);
        apply(task, req);
        return task; // changes are saved automatically when the transaction commits (dirty checking)
    }

    /** Checkbox behaviour: COMPLETED <-> PENDING. */
    @Transactional
    public StudyTask toggle(Long userId, Long id) {
        StudyTask task = find(userId, id);
        if (task.getStatus() == TaskStatus.COMPLETED) {
            task.setStatus(TaskStatus.PENDING);
            task.setCompletedAt(null);
        } else {
            task.setStatus(TaskStatus.COMPLETED);
            task.setCompletedAt(LocalDateTime.now());
        }
        return task;
    }

    /** Adds time to the task and records a study session so analytics stay in sync. */
    @Transactional
    public StudyTask logTime(Long userId, Long id, int minutes) {
        StudyTask task = find(userId, id);
        task.setActualMinutes(task.getActualMinutes() + minutes);
        if (task.getStatus() == TaskStatus.PENDING) {
            task.setStatus(TaskStatus.IN_PROGRESS);
        }
        StudySession session = new StudySession();
        session.setUser(users.getReferenceById(userId));
        session.setTask(task);
        session.setSubject(task.getSubject() == null || task.getSubject().isBlank() ? "General" : task.getSubject());
        session.setMinutes(minutes);
        session.setSessionDate(LocalDate.now());
        sessions.save(session);
        return task;
    }

    @Transactional
    public void delete(Long userId, Long id) {
        StudyTask task = find(userId, id);
        sessions.detachTask(task.getId());
        tasks.delete(task);
    }

    private StudyTask find(Long userId, Long id) {
        return tasks.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    private void apply(StudyTask task, TaskRequest req) {
        task.setTitle(req.title().trim());
        task.setDescription(req.description());
        task.setSubject(req.subject() == null ? null : req.subject().trim());
        if (req.priority() != null) task.setPriority(req.priority());
        task.setEstimatedMinutes(req.estimatedMinutes() == null ? 0 : req.estimatedMinutes());
        task.setScheduledDate(req.scheduledDate() == null ? LocalDate.now() : req.scheduledDate());
        TaskStatus status = req.status() == null ? task.getStatus() : req.status();
        task.setStatus(status);
        if (status == TaskStatus.COMPLETED) {
            if (task.getCompletedAt() == null) task.setCompletedAt(LocalDateTime.now());
        } else {
            task.setCompletedAt(null);
        }
    }
}
