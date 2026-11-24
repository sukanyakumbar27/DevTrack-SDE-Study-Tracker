package com.devtrack.repository;

import com.devtrack.entity.DsaProblem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DsaProblemRepository extends JpaRepository<DsaProblem, Long> {
    List<DsaProblem> findByUserIdOrderByIdDesc(Long userId);
    Optional<DsaProblem> findByIdAndUserId(Long id, Long userId);
}
