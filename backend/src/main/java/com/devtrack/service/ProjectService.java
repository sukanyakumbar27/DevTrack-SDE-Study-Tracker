package com.devtrack.service;

import com.devtrack.dto.RequestDtos.ProjectRequest;
import com.devtrack.dto.RequestDtos.ProjectTaskRequest;
import com.devtrack.dto.ResponseDtos.ProjectDto;
import com.devtrack.entity.Enums.ProjectStatus;
import com.devtrack.entity.Project;
import com.devtrack.entity.ProjectTask;
import com.devtrack.exception.ResourceNotFoundException;
import com.devtrack.repository.ProjectRepository;
import com.devtrack.repository.ProjectTaskRepository;
import com.devtrack.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProjectService {
    private final ProjectRepository projects;
    private final ProjectTaskRepository projectTasks;
    private final UserRepository users;

    public ProjectService(ProjectRepository projects, ProjectTaskRepository projectTasks, UserRepository users) {
        this.projects = projects;
        this.projectTasks = projectTasks;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<ProjectDto> list(Long userId) {
        Map<Long, List<ProjectTask>> byProject = projectTasks.findAllWithProject(userId).stream()
                .collect(Collectors.groupingBy(t -> t.getProject().getId()));
        return projects.findByUserIdOrderByIdDesc(userId).stream()
                .map(p -> toDto(p, byProject.getOrDefault(p.getId(), List.of())))
                .toList();
    }

    @Transactional
    public ProjectDto create(Long userId, ProjectRequest req) {
        Project p = new Project();
        p.setUser(users.getReferenceById(userId));
        apply(p, req);
        return toDto(projects.save(p), List.of());
    }

    @Transactional
    public ProjectDto update(Long userId, Long id, ProjectRequest req) {
        Project p = findProject(userId, id);
        apply(p, req);
        return toDto(p, projectTasks.findByProjectIdAndUserIdOrderByIdAsc(id, userId));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Project p = findProject(userId, id);
        projectTasks.deleteByProjectId(p.getId());
        projects.delete(p);
    }

    // ---- tasks inside a project ----
    @Transactional(readOnly = true)
    public List<ProjectTask> listTasks(Long userId, Long projectId) {
        findProject(userId, projectId);
        return projectTasks.findByProjectIdAndUserIdOrderByIdAsc(projectId, userId);
    }

    @Transactional
    public ProjectTask createTask(Long userId, Long projectId, ProjectTaskRequest req) {
        Project p = findProject(userId, projectId);
        ProjectTask t = new ProjectTask();
        t.setUser(users.getReferenceById(userId));
        t.setProject(p);
        applyTask(t, req);
        return projectTasks.save(t);
    }

    @Transactional
    public ProjectTask updateTask(Long userId, Long projectId, Long taskId, ProjectTaskRequest req) {
        ProjectTask t = projectTasks.findByIdAndProjectIdAndUserId(taskId, projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project task not found"));
        applyTask(t, req);
        return t;
    }

    @Transactional
    public void deleteTask(Long userId, Long projectId, Long taskId) {
        ProjectTask t = projectTasks.findByIdAndProjectIdAndUserId(taskId, projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project task not found"));
        projectTasks.delete(t);
    }

    private Project findProject(Long userId, Long id) {
        return projects.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    private void apply(Project p, ProjectRequest req) {
        p.setName(req.name().trim());
        p.setDescription(req.description());
        p.setGithubUrl(req.githubUrl());
        if (req.status() != null) p.setStatus(req.status());
    }

    private void applyTask(ProjectTask t, ProjectTaskRequest req) {
        t.setTitle(req.title().trim());
        t.setMilestone(Boolean.TRUE.equals(req.milestone()));
        t.setDone(Boolean.TRUE.equals(req.done()));
        t.setDueDate(req.dueDate());
    }

    private ProjectDto toDto(Project p, List<ProjectTask> tasks) {
        int total = tasks.size();
        int done = (int) tasks.stream().filter(ProjectTask::isDone).count();
        int percent = total == 0 ? 0 : (int) Math.round(100.0 * done / total);
        return new ProjectDto(p.getId(), p.getName(), p.getDescription(), p.getStatus(), p.getGithubUrl(),
                total, done, percent, p.getCreatedAt());
    }
}
