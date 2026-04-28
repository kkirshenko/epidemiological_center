package com.sanepidcenter.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing a sanitary inspection.
 */
@Entity
@Table(name = "inspections")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inspection {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    @JsonIgnoreProperties({"inspections", "createdAt", "updatedAt"})
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private InspectionType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspector_id", nullable = false)
    @JsonIgnoreProperties({"password", "createdAt", "updatedAt"})
    private Profile inspector;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "planned";

    @Column(name = "result", nullable = false, length = 20)
    @Builder.Default
    private String result = "pending";

    @Column(name = "findings_summary", nullable = false, columnDefinition = "text")
    private String findingsSummary;

    @Column(name = "recommendations", nullable = false, columnDefinition = "text")
    private String recommendations;

    @Column(name = "act_number", unique = true)
    private String actNumber;

    @OneToMany(mappedBy = "inspection", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<Violation> violations = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    @PrePersist
    private void ensureId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
