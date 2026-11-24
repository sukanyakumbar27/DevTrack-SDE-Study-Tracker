package com.devtrack.controller;

import com.devtrack.dto.RequestDtos.NoteRequest;
import com.devtrack.entity.Note;
import com.devtrack.entity.User;
import com.devtrack.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {
    private final NoteService service;

    public NoteController(NoteService service) {
        this.service = service;
    }

    @GetMapping
    public List<Note> list(@AuthenticationPrincipal User user) {
        return service.list(user.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Note create(@AuthenticationPrincipal User user, @Valid @RequestBody NoteRequest req) {
        return service.create(user.getId(), req);
    }

    @PutMapping("/{id}")
    public Note update(@AuthenticationPrincipal User user, @PathVariable Long id, @Valid @RequestBody NoteRequest req) {
        return service.update(user.getId(), id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal User user, @PathVariable Long id) {
        service.delete(user.getId(), id);
    }
}
