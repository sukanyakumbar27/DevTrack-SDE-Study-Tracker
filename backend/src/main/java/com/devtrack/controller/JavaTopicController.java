package com.devtrack.controller;

import com.devtrack.dto.RequestDtos.JavaTopicRequest;
import com.devtrack.entity.JavaTopic;
import com.devtrack.entity.User;
import com.devtrack.service.JavaTopicService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/java-topics")
public class JavaTopicController {
    private final JavaTopicService service;

    public JavaTopicController(JavaTopicService service) {
        this.service = service;
    }

    @GetMapping
    public List<JavaTopic> list(@AuthenticationPrincipal User user) {
        return service.list(user.getId());
    }

    @PutMapping("/{id}")
    public JavaTopic update(@AuthenticationPrincipal User user, @PathVariable Long id,
                            @Valid @RequestBody JavaTopicRequest req) {
        return service.update(user.getId(), id, req);
    }
}
