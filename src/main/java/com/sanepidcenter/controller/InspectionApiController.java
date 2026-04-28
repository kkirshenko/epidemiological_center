package com.sanepidcenter.controller;

import com.sanepidcenter.dto.InspectionApiDto;
import com.sanepidcenter.model.Inspection;
import com.sanepidcenter.service.InspectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inspections")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InspectionApiController {

    private final InspectionService inspectionService;

    @GetMapping
    public ResponseEntity<List<InspectionApiDto>> getAllInspections() {
        List<InspectionApiDto> result = inspectionService.getAllInspections()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InspectionApiDto> getInspection(@PathVariable UUID id) {
        return inspectionService.getInspectionById(id)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createInspection(@RequestBody Inspection inspection) {
        try {
            return ResponseEntity.ok(inspectionService.createInspection(inspection));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateInspection(@PathVariable UUID id, @RequestBody Inspection inspection) {
        try {
            return ResponseEntity.ok(inspectionService.updateInspection(id, inspection));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInspection(@PathVariable UUID id) {
        inspectionService.deleteInspection(id);
        return ResponseEntity.noContent().build();
    }

    private InspectionApiDto toDto(Inspection inspection) {
        return InspectionApiDto.builder()
                .id(inspection.getId())
                .organizationId(inspection.getOrganization() != null ? inspection.getOrganization().getId() : null)
                .organizationName(inspection.getOrganization() != null ? inspection.getOrganization().getName() : null)
                .typeId(inspection.getType() != null ? inspection.getType().getId() : null)
                .typeName(inspection.getType() != null ? inspection.getType().getName() : null)
                .inspectorId(inspection.getInspector() != null ? inspection.getInspector().getId() : null)
                .inspectorName(inspection.getInspector() != null ? inspection.getInspector().getFullName() : null)
                .scheduledDate(inspection.getScheduledDate())
                .startDate(inspection.getStartDate())
                .endDate(inspection.getEndDate())
                .status(inspection.getStatus())
                .result(inspection.getResult())
                .findingsSummary(inspection.getFindingsSummary())
                .recommendations(inspection.getRecommendations())
                .actNumber(inspection.getActNumber())
                .createdAt(inspection.getCreatedAt())
                .updatedAt(inspection.getUpdatedAt())
                .build();
    }
}
