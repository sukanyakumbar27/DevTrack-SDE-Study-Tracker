package com.devtrack.controller;

import com.devtrack.dto.RequestDtos.SessionRequest;
import com.devtrack.entity.StudySession;
import com.devtrack.entity.User;
import com.devtrack.service.StudySessionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class StudySessionController {
    private final StudySessionService service;

    public StudySessionController(StudySessionService service) {
        this.service = service;
    }

    @GetMapping
    public List<StudySession> list(@AuthenticationPrincipal User user,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.list(user.getId(), from, to);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudySession create(@AuthenticationPrincipal User user, @Valid @RequestBody SessionRequest req) {
        return service.create(user.getId(), req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal User user, @PathVariable Long id) {
        service.delete(user.getId(), id);
    }
}
