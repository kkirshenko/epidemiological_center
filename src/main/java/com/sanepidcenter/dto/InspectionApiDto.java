package com.sanepidcenter.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class InspectionApiDto {
    private UUID id;
    private UUID organizationId;
    private String organizationName;
    private Integer typeId;
    private String typeName;
    private UUID inspectorId;
    private String inspectorName;
    private LocalDate scheduledDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String result;
    private String findingsSummary;
    private String recommendations;
    private String actNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
