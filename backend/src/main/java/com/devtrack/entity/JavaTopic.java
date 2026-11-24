package com.devtrack.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "java_topics")
@Getter @Setter @NoArgsConstructor
public class JavaTopic extends OwnedEntity {
    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private boolean completed = false;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;
}
