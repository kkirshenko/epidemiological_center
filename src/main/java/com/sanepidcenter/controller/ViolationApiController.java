package com.sanepidcenter.controller;

import com.sanepidcenter.dto.ViolationApiDto;
import com.sanepidcenter.model.Violation;
import com.sanepidcenter.service.ViolationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/violations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ViolationApiController {

    private final ViolationService violationService;

    @GetMapping
    public ResponseEntity<List<ViolationApiDto>> getAllViolations() {
        List<ViolationApiDto> result = violationService.getAllViolations()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ViolationApiDto> getViolation(@PathVariable UUID id) {
        return violationService.getViolationById(id)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createViolation(@RequestBody Violation violation) {
        try {
            return ResponseEntity.ok(violationService.createViolation(violation));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateViolation(@PathVariable UUID id, @RequestBody Violation violationDetails) {
        try {
            Violation updated = violationService.updateViolation(id, violationDetails);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteViolation(@PathVariable UUID id) {
        violationService.deleteViolation(id);
        return ResponseEntity.noContent().build();
    }

    private ViolationApiDto toDto(Violation violation) {
        return ViolationApiDto.builder()
                .id(violation.getId())
                .inspectionId(violation.getInspection() != null ? violation.getInspection().getId() : null)
                .inspectionActNumber(violation.getInspection() != null ? violation.getInspection().getActNumber() : null)
                .organizationId(violation.getInspection() != null && violation.getInspection().getOrganization() != null
                        ? violation.getInspection().getOrganization().getId() : null)
                .organizationName(violation.getInspection() != null && violation.getInspection().getOrganization() != null
                        ? violation.getInspection().getOrganization().getName() : null)
                .code(violation.getCode())
                .description(violation.getDescription())
                .severity(violation.getSeverity())
                .articleReference(violation.getArticleReference())
                .correctionDeadline(violation.getCorrectionDeadline())
                .resolved(violation.getResolved())
                .resolutionNotes(violation.getResolutionNotes())
                .createdAt(violation.getCreatedAt())
                .updatedAt(violation.getUpdatedAt())
                .build();
    }
}
