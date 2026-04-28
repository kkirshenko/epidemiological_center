package com.sanepidcenter.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ViolationApiDto {
    private UUID id;
    private UUID inspectionId;
    private String inspectionActNumber;
    private UUID organizationId;
    private String organizationName;
    private String code;
    private String description;
    private String severity;
    private String articleReference;
    private LocalDate correctionDeadline;
    private Boolean resolved;
    private String resolutionNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
