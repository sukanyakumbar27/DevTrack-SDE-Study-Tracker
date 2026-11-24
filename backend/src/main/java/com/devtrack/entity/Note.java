package com.devtrack.entity;

import com.devtrack.entity.Enums.NoteCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notes")
@Getter @Setter @NoArgsConstructor
public class Note extends OwnedEntity {
    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private NoteCategory category = NoteCategory.CODING;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
