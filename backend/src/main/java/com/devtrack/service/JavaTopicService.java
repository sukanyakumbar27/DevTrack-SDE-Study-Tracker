package com.devtrack.service;

import com.devtrack.dto.RequestDtos.JavaTopicRequest;
import com.devtrack.entity.JavaTopic;
import com.devtrack.exception.ResourceNotFoundException;
import com.devtrack.repository.JavaTopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JavaTopicService {
    private final JavaTopicRepository topics;

    public JavaTopicService(JavaTopicRepository topics) {
        this.topics = topics;
    }

    @Transactional(readOnly = true)
    public List<JavaTopic> list(Long userId) {
        return topics.findByUserIdOrderBySortOrderAsc(userId);
    }

    @Transactional
    public JavaTopic update(Long userId, Long id, JavaTopicRequest req) {
        JavaTopic t = topics.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Java topic not found"));
        if (req.completed() != null && req.completed() != t.isCompleted()) {
            t.setCompleted(req.completed());
            t.setCompletedAt(req.completed() ? LocalDateTime.now() : null);
        }
        if (req.notes() != null) {
            t.setNotes(req.notes());
        }
        return t;
    }
}
