package com.devtrack.repository;

import com.devtrack.entity.StudyTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudyTaskRepository extends JpaRepository<StudyTask, Long> {
    List<StudyTask> findByUserIdOrderByScheduledDateDescIdDesc(Long userId);
    Optional<StudyTask> findByIdAndUserId(Long id, Long userId);
}
