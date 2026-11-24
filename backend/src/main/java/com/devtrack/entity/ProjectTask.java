package com.devtrack.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** A task inside a project. When milestone = true it is displayed as a milestone. */
@Entity
@Table(name = "project_tasks")
@Getter @Setter @NoArgsConstructor
public class ProjectTask extends OwnedEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnore
    private Project project;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false)
    private boolean milestone = false;

    @Column(nullable = false)
    private boolean done = false;

    @Column(name = "due_date")
    private LocalDate dueDate;
}
