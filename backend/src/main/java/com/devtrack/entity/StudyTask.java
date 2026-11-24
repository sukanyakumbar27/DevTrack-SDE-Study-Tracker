package com.devtrack.entity;

import com.devtrack.entity.Enums.Priority;
import com.devtrack.entity.Enums.TaskStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "study_tasks")
@Getter @Setter @NoArgsConstructor
public class StudyTask extends OwnedEntity {
    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 80)
    private String subject;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private TaskStatus status = TaskStatus.PENDING;

    @Column(name = "estimated_minutes", nullable = false)
    private int estimatedMinutes = 0;

    @Column(name = "actual_minutes", nullable = false)
    private int actualMinutes = 0;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate = LocalDate.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
