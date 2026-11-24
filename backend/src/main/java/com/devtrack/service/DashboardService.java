package com.devtrack.service;

import com.devtrack.dto.ResponseDtos.DashboardDto;
import com.devtrack.entity.DsaProblem;
import com.devtrack.entity.Enums.ProblemStatus;
import com.devtrack.entity.Enums.TaskStatus;
import com.devtrack.entity.StudySession;
import com.devtrack.entity.StudyTask;
import com.devtrack.repository.DsaProblemRepository;
import com.devtrack.repository.StudySessionRepository;
import com.devtrack.repository.StudyTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class DashboardService {
    private final StudyTaskRepository tasks;
    private final StudySessionRepository sessions;
    private final DsaProblemRepository dsa;
    private final StatsCalculator stats;

    public DashboardService(StudyTaskRepository tasks, StudySessionRepository sessions,
                            DsaProblemRepository dsa, StatsCalculator stats) {
        this.tasks = tasks;
        this.sessions = sessions;
        this.dsa = dsa;
        this.stats = stats;
    }

    @Transactional(readOnly = true)
    public DashboardDto build(Long userId) {
        LocalDate today = LocalDate.now();
        List<StudyTask> allTasks = tasks.findByUserIdOrderByScheduledDateDescIdDesc(userId);
        List<StudySession> allSessions = sessions.findByUserIdOrderBySessionDateDescIdDesc(userId);
        List<DsaProblem> problems = dsa.findByUserIdOrderByIdDesc(userId);

        List<StudyTask> todayTasks = allTasks.stream().filter(t -> t.getScheduledDate().equals(today)).toList();
        long doneToday = todayTasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
        long pendingToday = todayTasks.size() - doneToday;
        int productivity = todayTasks.isEmpty() ? 0 : (int) Math.round(100.0 * doneToday / todayTasks.size());

        int totalMinutes = allSessions.stream().mapToInt(StudySession::getMinutes).sum();
        int todayMinutes = allSessions.stream().filter(s -> s.getSessionDate().equals(today))
                .mapToInt(StudySession::getMinutes).sum();

        long totalCompleted = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
        long dsaSolved = problems.stream().filter(p -> p.getStatus() == ProblemStatus.SOLVED).count();

        return new DashboardDto(
                today,
                StatsCalculator.round1(totalMinutes / 60.0),
                StatsCalculator.round1(todayMinutes / 60.0),
                doneToday,
                pendingToday,
                productivity,
                stats.dayPoints(allTasks, allSessions, today.minusDays(6), today),
                stats.streak(allTasks, allSessions, today),
                dsaSolved,
                totalCompleted,
                allTasks.size() - totalCompleted);
    }
}
