package com.devtrack.service;

import com.devtrack.dto.RequestDtos.SessionRequest;
import com.devtrack.entity.StudySession;
import com.devtrack.exception.ResourceNotFoundException;
import com.devtrack.repository.StudySessionRepository;
import com.devtrack.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class StudySessionService {
    private final StudySessionRepository sessions;
    private final UserRepository users;

    public StudySessionService(StudySessionRepository sessions, UserRepository users) {
        this.sessions = sessions;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<StudySession> list(Long userId, LocalDate from, LocalDate to) {
        return sessions.findByUserIdOrderBySessionDateDescIdDesc(userId).stream()
                .filter(s -> from == null || !s.getSessionDate().isBefore(from))
                .filter(s -> to == null || !s.getSessionDate().isAfter(to))
                .toList();
    }

    @Transactional
    public StudySession create(Long userId, SessionRequest req) {
        StudySession s = new StudySession();
        s.setUser(users.getReferenceById(userId));
        s.setSubject(req.subject().trim());
        s.setMinutes(req.minutes());
        s.setSessionDate(req.sessionDate() == null ? LocalDate.now() : req.sessionDate());
        return sessions.save(s);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        StudySession s = sessions.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Study session not found"));
        sessions.delete(s);
    }
}
