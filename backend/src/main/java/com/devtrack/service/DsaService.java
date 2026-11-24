package com.devtrack.service;

import com.devtrack.dto.RequestDtos.DsaRequest;
import com.devtrack.dto.ResponseDtos.DsaSummary;
import com.devtrack.dto.ResponseDtos.NameValue;
import com.devtrack.entity.DsaProblem;
import com.devtrack.entity.Enums.Difficulty;
import com.devtrack.entity.Enums.DsaTopic;
import com.devtrack.entity.Enums.ProblemStatus;
import com.devtrack.exception.ResourceNotFoundException;
import com.devtrack.repository.DsaProblemRepository;
import com.devtrack.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
public class DsaService {
    private final DsaProblemRepository problems;
    private final UserRepository users;

    public DsaService(DsaProblemRepository problems, UserRepository users) {
        this.problems = problems;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<DsaProblem> list(Long userId) {
        return problems.findByUserIdOrderByIdDesc(userId);
    }

    @Transactional
    public DsaProblem create(Long userId, DsaRequest req) {
        DsaProblem p = new DsaProblem();
        p.setUser(users.getReferenceById(userId));
        apply(p, req);
        return problems.save(p);
    }

    @Transactional
    public DsaProblem update(Long userId, Long id, DsaRequest req) {
        DsaProblem p = find(userId, id);
        apply(p, req);
        return p;
    }

    @Transactional
    public void delete(Long userId, Long id) {
        problems.delete(find(userId, id));
    }

    @Transactional(readOnly = true)
    public DsaSummary summary(Long userId) {
        List<DsaProblem> all = problems.findByUserIdOrderByIdDesc(userId);
        List<DsaProblem> solved = all.stream().filter(p -> p.getStatus() == ProblemStatus.SOLVED).toList();
        List<NameValue> byTopic = Arrays.stream(DsaTopic.values())
                .map(t -> new NameValue(t.name(), solved.stream().filter(p -> p.getTopic() == t).count()))
                .toList();
        List<NameValue> byDifficulty = Arrays.stream(Difficulty.values())
                .map(d -> new NameValue(d.name(), solved.stream().filter(p -> p.getDifficulty() == d).count()))
                .toList();
        return new DsaSummary(solved.size(), all.size() - solved.size(), byTopic, byDifficulty);
    }

    private DsaProblem find(Long userId, Long id) {
        return problems.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found"));
    }

    private void apply(DsaProblem p, DsaRequest req) {
        p.setName(req.name().trim());
        p.setPlatform(req.platform());
        p.setTopic(req.topic());
        p.setDifficulty(req.difficulty());
        p.setStatus(req.status());
        p.setNotes(req.notes());
        // A solved problem always has a date; default to today if the client did not send one.
        p.setSolvedDate(req.status() == ProblemStatus.SOLVED && req.solvedDate() == null
                ? LocalDate.now() : req.solvedDate());
    }
}
