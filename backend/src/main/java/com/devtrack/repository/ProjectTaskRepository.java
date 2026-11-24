package com.devtrack.repository;

import com.devtrack.entity.ProjectTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectTaskRepository extends JpaRepository<ProjectTask, Long> {
    List<ProjectTask> findByProjectIdAndUserIdOrderByIdAsc(Long projectId, Long userId);
    Optional<ProjectTask> findByIdAndProjectIdAndUserId(Long id, Long projectId, Long userId);
    void deleteByProjectId(Long projectId);

    /** One query for all of a user's project tasks (avoids N+1 queries on the project list). */
    @Query("select t from ProjectTask t join fetch t.project where t.user.id = :uid")
    List<ProjectTask> findAllWithProject(@Param("uid") Long uid);
}
