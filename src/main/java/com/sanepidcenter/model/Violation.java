package com.sanepidcenter.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a violation found during an inspection.
 */
@Entity
@Table(name = "violations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Violation {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspection_id", nullable = false)
    private Inspection inspection;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "severity", nullable = false, length = 20)
    @Builder.Default
    private String severity = "minor";

    @Column(name = "article_reference", nullable = false)
    private String articleReference;

    @Column(name = "correction_deadline")
    private LocalDate correctionDeadline;

    @Column(name = "resolved", nullable = false)
    @Builder.Default
    private Boolean resolved = false;

    @Column(name = "resolution_notes", nullable = false, columnDefinition = "text")
    private String resolutionNotes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    private void ensureId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
