package com.devtrack.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** One block of focused study time. Dashboard and analytics hours come from this table. */
@Entity
@Table(name = "study_sessions")
@Getter @Setter @NoArgsConstructor
public class StudySession extends OwnedEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    @JsonIgnore
    private StudyTask task;

    @Column(nullable = false, length = 80)
    private String subject;

    @Column(nullable = false)
    private int minutes;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate = LocalDate.now();
}
