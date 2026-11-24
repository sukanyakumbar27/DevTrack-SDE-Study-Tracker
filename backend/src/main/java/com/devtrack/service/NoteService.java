package com.devtrack.service;

import com.devtrack.dto.RequestDtos.NoteRequest;
import com.devtrack.entity.Note;
import com.devtrack.exception.ResourceNotFoundException;
import com.devtrack.repository.NoteRepository;
import com.devtrack.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoteService {
    private final NoteRepository notes;
    private final UserRepository users;

    public NoteService(NoteRepository notes, UserRepository users) {
        this.notes = notes;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<Note> list(Long userId) {
        return notes.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    @Transactional
    public Note create(Long userId, NoteRequest req) {
        Note n = new Note();
        n.setUser(users.getReferenceById(userId));
        apply(n, req);
        return notes.save(n);
    }

    @Transactional
    public Note update(Long userId, Long id, NoteRequest req) {
        Note n = find(userId, id);
        apply(n, req);
        return n;
    }

    @Transactional
    public void delete(Long userId, Long id) {
        notes.delete(find(userId, id));
    }

    private Note find(Long userId, Long id) {
        return notes.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));
    }

    private void apply(Note n, NoteRequest req) {
        n.setTitle(req.title().trim());
        n.setContent(req.content());
        if (req.category() != null) n.setCategory(req.category());
        n.setUpdatedAt(LocalDateTime.now());
    }
}
