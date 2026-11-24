package com.devtrack.repository;

import com.devtrack.entity.JavaTopic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JavaTopicRepository extends JpaRepository<JavaTopic, Long> {
    List<JavaTopic> findByUserIdOrderBySortOrderAsc(Long userId);
    Optional<JavaTopic> findByIdAndUserId(Long id, Long userId);
}
