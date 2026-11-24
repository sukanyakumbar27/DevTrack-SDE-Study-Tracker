package com.devtrack.service;

import com.devtrack.dto.ResponseDtos.*;
import com.devtrack.entity.DsaProblem;
import com.devtrack.entity.Enums.Difficulty;
import com.devtrack.entity.Enums.DsaTopic;
import com.devtrack.entity.Enums.ProblemStatus;
import com.devtrack.entity.Enums.TaskStatus;
import com.devtrack.entity.StudySession;
import com.devtrack.entity.StudyTask;
import com.devtrack.repository.DsaProblemRepository;
import com.devtrack.repository.StudySessionRepository;
import com.devtrack.repository.StudyTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {
    private final StudyTaskRepository tasks;
    private final StudySessionRepository sessions;
    private final DsaProblemRepository dsa;
    private final StatsCalculator stats;

    public AnalyticsService(StudyTaskRepository tasks, StudySessionRepository sessions,
                            DsaProblemRepository dsa, StatsCalculator stats) {
        this.tasks = tasks;
        this.sessions = sessions;
        this.dsa = dsa;
        this.stats = stats;
    }

    @Transactional(readOnly = true)
    public AnalyticsDto build(Long userId) {
        LocalDate today = LocalDate.now();
        List<StudyTask> allTasks = tasks.findByUserIdOrderByScheduledDateDescIdDesc(userId);
        List<StudySession> allSessions = sessions.findByUserIdOrderBySessionDateDescIdDesc(userId);
        List<DsaProblem> solved = dsa.findByUserIdOrderByIdDesc(userId).stream()
                .filter(p -> p.getStatus() == ProblemStatus.SOLVED).toList();

        // Daily productivity: last 14 days
        List<DayPoint> daily = stats.dayPoints(allTasks, allSessions, today.minusDays(13), today);

        // Weekly study hours: last 8 weeks (weeks start on Monday)
        List<WeekPoint> weekly = new ArrayList<>();
        LocalDate thisMonday = today.with(DayOfWeek.MONDAY);
        for (int i = 7; i >= 0; i--) {
            LocalDate start = thisMonday.minusWeeks(i);
            LocalDate end = start.plusDays(6);
            int minutes = allSessions.stream()
                    .filter(s -> !s.getSessionDate().isBefore(start) && !s.getSessionDate().isAfter(end))
                    .mapToInt(StudySession::getMinutes).sum();
            long completed = allTasks.stream()
                    .filter(t -> t.getStatus() == TaskStatus.COMPLETED
                            && !t.getScheduledDate().isBefore(start) && !t.getScheduledDate().isAfter(end))
                    .count();
            weekly.add(new WeekPoint(start, StatsCalculator.round1(minutes / 60.0), completed));
        }

        // Monthly progress: last 6 months
        List<MonthPoint> monthly = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.from(today).minusMonths(i);
            long planned = allTasks.stream().filter(t -> YearMonth.from(t.getScheduledDate()).equals(ym)).count();
            long completed = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED
                    && YearMonth.from(t.getScheduledDate()).equals(ym)).count();
            long dsaCount = solved.stream().filter(p -> p.getSolvedDate() != null
                    && YearMonth.from(p.getSolvedDate()).equals(ym)).count();
            int minutes = allSessions.stream().filter(s -> YearMonth.from(s.getSessionDate()).equals(ym))
                    .mapToInt(StudySession::getMinutes).sum();
            monthly.add(new MonthPoint(ym.toString(), planned, completed, dsaCount,
                    StatsCalculator.round1(minutes / 60.0)));
        }

        // DSA charts
        List<NameValue> byTopic = Arrays.stream(DsaTopic.values())
                .map(t -> new NameValue(t.name(), solved.stream().filter(p -> p.getTopic() == t).count())).toList();
        List<NameValue> byDifficulty = Arrays.stream(Difficulty.values())
                .map(d -> new NameValue(d.name(), solved.stream().filter(p -> p.getDifficulty() == d).count())).toList();

        // Subject-wise study distribution (hours)
        Map<String, Integer> bySubject = allSessions.stream().collect(
                Collectors.groupingBy(StudySession::getSubject, Collectors.summingInt(StudySession::getMinutes)));
        List<NameValue> subjects = bySubject.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(e -> new NameValue(e.getKey(), StatsCalculator.round1(e.getValue() / 60.0)))
                .toList();

        long completedTotal = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();

        // Improvement over time: last 7 days compared with the 7 days before
        List<DayPoint> last7 = stats.dayPoints(allTasks, allSessions, today.minusDays(6), today);
        List<DayPoint> prev7 = stats.dayPoints(allTasks, allSessions, today.minusDays(13), today.minusDays(7));
        double productivityDelta = StatsCalculator.round1(avgPercent(last7) - avgPercent(prev7));
        double hoursDelta = StatsCalculator.round1(
                last7.stream().mapToDouble(DayPoint::hours).sum() - prev7.stream().mapToDouble(DayPoint::hours).sum());

        return new AnalyticsDto(daily, weekly, monthly, byTopic, byDifficulty, subjects,
                completedTotal, allTasks.size() - completedTotal, productivityDelta, hoursDelta);
    }

    /** Average completion % over the days that actually had tasks. */
    private double avgPercent(List<DayPoint> points) {
        return points.stream().filter(p -> p.total() > 0).mapToInt(DayPoint::completionPercent).average().orElse(0);
    }
}
