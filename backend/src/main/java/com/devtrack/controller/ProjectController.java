package com.devtrack.controller;

import com.devtrack.dto.RequestDtos.ProjectRequest;
import com.devtrack.dto.RequestDtos.ProjectTaskRequest;
import com.devtrack.dto.ResponseDtos.ProjectDto;
import com.devtrack.entity.ProjectTask;
import com.devtrack.entity.User;
import com.devtrack.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProjectDto> list(@AuthenticationPrincipal User user) {
        return service.list(user.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectDto create(@AuthenticationPrincipal User user, @Valid @RequestBody ProjectRequest req) {
        return service.create(user.getId(), req);
    }

    @PutMapping("/{id}")
    public ProjectDto update(@AuthenticationPrincipal User user, @PathVariable Long id,
                             @Valid @RequestBody ProjectRequest req) {
        return service.update(user.getId(), id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal User user, @PathVariable Long id) {
        service.delete(user.getId(), id);
    }

    @GetMapping("/{id}/tasks")
    public List<ProjectTask> tasks(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return service.listTasks(user.getId(), id);
    }

    @PostMapping("/{id}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectTask createTask(@AuthenticationPrincipal User user, @PathVariable Long id,
                                  @Valid @RequestBody ProjectTaskRequest req) {
        return service.createTask(user.getId(), id, req);
    }

    @PutMapping("/{id}/tasks/{taskId}")
    public ProjectTask updateTask(@AuthenticationPrincipal User user, @PathVariable Long id,
                                  @PathVariable Long taskId, @Valid @RequestBody ProjectTaskRequest req) {
        return service.updateTask(user.getId(), id, taskId, req);
    }

    @DeleteMapping("/{id}/tasks/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@AuthenticationPrincipal User user, @PathVariable Long id, @PathVariable Long taskId) {
        service.deleteTask(user.getId(), id, taskId);
    }
}
