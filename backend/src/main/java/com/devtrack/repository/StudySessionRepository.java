package com.devtrack.repository;

import com.devtrack.entity.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudySessionRepository extends JpaRepository<StudySession, Long> {
    List<StudySession> findByUserIdOrderBySessionDateDescIdDesc(Long userId);
    Optional<StudySession> findByIdAndUserId(Long id, Long userId);

    /** Keep the study history when a task is deleted: just unlink the task. */
    @Modifying(clearAutomatically = true)
    @Query("update StudySession s set s.task = null where s.task.id = :taskId")
    void detachTask(@Param("taskId") Long taskId);
}
