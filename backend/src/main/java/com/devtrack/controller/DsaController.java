package com.devtrack.controller;

import com.devtrack.dto.RequestDtos.DsaRequest;
import com.devtrack.dto.ResponseDtos.DsaSummary;
import com.devtrack.entity.DsaProblem;
import com.devtrack.entity.User;
import com.devtrack.service.DsaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dsa")
public class DsaController {
    private final DsaService service;

    public DsaController(DsaService service) {
        this.service = service;
    }

    @GetMapping
    public List<DsaProblem> list(@AuthenticationPrincipal User user) {
        return service.list(user.getId());
    }

    @GetMapping("/summary")
    public DsaSummary summary(@AuthenticationPrincipal User user) {
        return service.summary(user.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DsaProblem create(@AuthenticationPrincipal User user, @Valid @RequestBody DsaRequest req) {
        return service.create(user.getId(), req);
    }

    @PutMapping("/{id}")
    public DsaProblem update(@AuthenticationPrincipal User user, @PathVariable Long id,
                             @Valid @RequestBody DsaRequest req) {
        return service.update(user.getId(), id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal User user, @PathVariable Long id) {
        service.delete(user.getId(), id);
    }
}
