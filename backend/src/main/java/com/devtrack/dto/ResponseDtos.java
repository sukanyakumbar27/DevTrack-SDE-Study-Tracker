package com.devtrack.dto;

import com.devtrack.entity.Enums.ProjectStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Output DTOs for computed data that is not a plain database row. */
public final class ResponseDtos {
    private ResponseDtos() {}

    /** One day of stats; used by the dashboard weekly chart and the analytics daily chart. */
    public record DayPoint(LocalDate date, double hours, long completed, long total, int completionPercent) {}

    public record WeekPoint(LocalDate weekStart, double hours, long tasksCompleted) {}

    public record MonthPoint(String month, long tasksPlanned, long tasksCompleted, long dsaSolved, double hours) {}

    public record NameValue(String name, double value) {}

    public record DashboardDto(
            LocalDate today,
            double totalStudyHours,
            double todayStudyHours,
            long tasksCompletedToday,
            long tasksPendingToday,
            int productivityPercent,
            List<DayPoint> weeklyProgress,
            int streak,
            long dsaSolved,
            long totalTasksCompleted,
            long totalTasksPending) {}

    public record AnalyticsDto(
            List<DayPoint> daily,
            List<WeekPoint> weekly,
            List<MonthPoint> monthly,
            List<NameValue> dsaByTopic,
            List<NameValue> dsaByDifficulty,
            List<NameValue> subjects,
            long completedTasks,
            long pendingTasks,
            double productivityDelta,
            double hoursDelta) {}

    public record DsaSummary(long totalSolved, long totalAttempted, List<NameValue> byTopic, List<NameValue> byDifficulty) {}

    public record ProjectDto(
            Long id, String name, String description, ProjectStatus status, String githubUrl,
            int totalTasks, int doneTasks, int completionPercent, LocalDateTime createdAt) {}
}
