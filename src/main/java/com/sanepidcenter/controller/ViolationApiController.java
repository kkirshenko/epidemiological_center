package com.sanepidcenter.controller;

import com.sanepidcenter.model.Violation;
import com.sanepidcenter.service.ViolationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/violations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ViolationApiController {

    private final ViolationService violationService;

    @GetMapping
    public ResponseEntity<List<Violation>> getAllViolations() {
        return ResponseEntity.ok(violationService.getAllViolations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Violation> getViolation(@PathVariable UUID id) {
        return violationService.getViolationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','LABORANT')")
    public ResponseEntity<?> createViolation(@RequestBody Violation violation) {
        try {
            return ResponseEntity.ok(violationService.createViolation(violation));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','LABORANT')")
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
    @PreAuthorize("hasAnyRole('ADMIN','LABORANT')")
    public ResponseEntity<Void> deleteViolation(@PathVariable UUID id) {
        violationService.deleteViolation(id);
        return ResponseEntity.noContent().build();
    }
}
