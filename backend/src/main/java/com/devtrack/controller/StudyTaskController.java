package com.devtrack.controller;

import com.devtrack.dto.RequestDtos.TaskRequest;
import com.devtrack.dto.RequestDtos.TimeLogRequest;
import com.devtrack.entity.Enums.TaskStatus;
import com.devtrack.entity.StudyTask;
import com.devtrack.entity.User;
import com.devtrack.service.StudyTaskService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class StudyTaskController {
    private final StudyTaskService service;

    public StudyTaskController(StudyTaskService service) {
        this.service = service;
    }

    @GetMapping
    public List<StudyTask> list(@AuthenticationPrincipal User user,
                                @RequestParam(required = false) TaskStatus status,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.list(user.getId(), status, from, to);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudyTask create(@AuthenticationPrincipal User user, @Valid @RequestBody TaskRequest req) {
        return service.create(user.getId(), req);
    }

    @PutMapping("/{id}")
    public StudyTask update(@AuthenticationPrincipal User user, @PathVariable Long id,
                            @Valid @RequestBody TaskRequest req) {
        return service.update(user.getId(), id, req);
    }

    @PatchMapping("/{id}/toggle")
    public StudyTask toggle(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return service.toggle(user.getId(), id);
    }

    @PostMapping("/{id}/log-time")
    public StudyTask logTime(@AuthenticationPrincipal User user, @PathVariable Long id,
                             @Valid @RequestBody TimeLogRequest req) {
        return service.logTime(user.getId(), id, req.minutes());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal User user, @PathVariable Long id) {
        service.delete(user.getId(), id);
    }
}
