package com.devtrack.entity;

import com.devtrack.entity.Enums.Difficulty;
import com.devtrack.entity.Enums.DsaTopic;
import com.devtrack.entity.Enums.ProblemStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "dsa_problems")
@Getter @Setter @NoArgsConstructor
public class DsaProblem extends OwnedEntity {
    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 80)
    private String platform;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private DsaTopic topic;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 15)
    private ProblemStatus status = ProblemStatus.SOLVED;

    @Column(name = "solved_date")
    private LocalDate solvedDate;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
