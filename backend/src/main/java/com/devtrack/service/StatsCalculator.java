package com.devtrack.service;

import com.devtrack.dto.ResponseDtos.DayPoint;
import com.devtrack.entity.Enums.TaskStatus;
import com.devtrack.entity.StudySession;
import com.devtrack.entity.StudyTask;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/** Pure calculation helpers shared by the dashboard and analytics services. */
@Component
public class StatsCalculator {

    public static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    /** One DayPoint per day between from and to (inclusive). */
    public List<DayPoint> dayPoints(List<StudyTask> tasks, List<StudySession> sessions, LocalDate from, LocalDate to) {
        Map<LocalDate, Integer> minutesByDay = sessions.stream().collect(
                Collectors.groupingBy(StudySession::getSessionDate, Collectors.summingInt(StudySession::getMinutes)));
        Map<LocalDate, List<StudyTask>> tasksByDay = tasks.stream()
                .collect(Collectors.groupingBy(StudyTask::getScheduledDate));

        List<DayPoint> points = new ArrayList<>();
        for (LocalDate day = from; !day.isAfter(to); day = day.plusDays(1)) {
            List<StudyTask> dayTasks = tasksByDay.getOrDefault(day, List.of());
            long done = dayTasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
            int percent = dayTasks.isEmpty() ? 0 : (int) Math.round(100.0 * done / dayTasks.size());
            double hours = round1(minutesByDay.getOrDefault(day, 0) / 60.0);
            points.add(new DayPoint(day, hours, done, dayTasks.size(), percent));
        }
        return points;
    }

    /**
     * Study streak = number of consecutive days with study activity (a session logged or a task completed),
     * counting back from today. If today has no activity yet, the streak is still alive from yesterday.
     */
    public int streak(List<StudyTask> tasks, List<StudySession> sessions, LocalDate today) {
        Set<LocalDate> active = new HashSet<>();
        sessions.forEach(s -> active.add(s.getSessionDate()));
        tasks.stream().filter(t -> t.getCompletedAt() != null)
                .forEach(t -> active.add(t.getCompletedAt().toLocalDate()));
        LocalDate cursor = active.contains(today) ? today : today.minusDays(1);
        int streak = 0;
        while (active.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }
}
