package com.devtrack.dto;

import com.devtrack.entity.Enums.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

/** Input DTOs (what the client is allowed to send). Validation annotations are checked by @Valid. */
public final class RequestDtos {
    private RequestDtos() {}

    public record TaskRequest(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 2000) String description,
            @Size(max = 80) String subject,
            Priority priority,
            TaskStatus status,
            @Min(0) @Max(1440) Integer estimatedMinutes,
            LocalDate scheduledDate) {}

    public record TimeLogRequest(@Min(1) @Max(1440) int minutes) {}

    public record SessionRequest(
            @NotBlank @Size(max = 80) String subject,
            @Min(1) @Max(1440) int minutes,
            LocalDate sessionDate) {}

    public record DsaRequest(
            @NotBlank @Size(max = 200) String name,
            @Size(max = 80) String platform,
            @NotNull DsaTopic topic,
            @NotNull Difficulty difficulty,
            @NotNull ProblemStatus status,
            LocalDate solvedDate,
            @Size(max = 5000) String notes) {}

    public record JavaTopicRequest(Boolean completed, @Size(max = 5000) String notes) {}

    public record ProjectRequest(
            @NotBlank @Size(max = 150) String name,
            @Size(max = 2000) String description,
            ProjectStatus status,
            @Size(max = 300) String githubUrl) {}

    public record ProjectTaskRequest(
            @NotBlank @Size(max = 200) String title,
            Boolean milestone,
            Boolean done,
            LocalDate dueDate) {}

    public record NoteRequest(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 20000) String content,
            NoteCategory category) {}
}
