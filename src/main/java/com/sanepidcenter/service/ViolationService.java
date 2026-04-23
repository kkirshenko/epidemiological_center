package com.sanepidcenter.service;

import com.sanepidcenter.model.Inspection;
import com.sanepidcenter.model.Violation;
import com.sanepidcenter.repository.InspectionRepository;
import com.sanepidcenter.repository.ViolationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ViolationService {

    private final ViolationRepository violationRepository;
    private final InspectionRepository inspectionRepository;

    public List<Violation> getAllViolations() {
        return violationRepository.findAll();
    }

    public List<Violation> searchAllFields(String query) {
        return violationRepository.findAllFieldsContainingIgnoreCase(query);
    }

    public List<Violation> sortViolations(List<Violation> violations, String sortBy, String sortDir) {
        if (sortBy == null || sortBy.isEmpty()) {
            return violations;
        }
        
        boolean ascending = sortDir == null || "asc".equalsIgnoreCase(sortDir);
        
        return violations.stream()
            .sorted((v1, v2) -> {
                int cmp = 0;
                switch (sortBy.toLowerCase()) {
                    case "inspection":
                        String insp1 = v1.getInspection() != null && v1.getInspection().getOrganization() != null 
                            ? v1.getInspection().getOrganization().getName() : "";
                        String insp2 = v2.getInspection() != null && v2.getInspection().getOrganization() != null 
                            ? v2.getInspection().getOrganization().getName() : "";
                        cmp = insp1.compareToIgnoreCase(insp2);
                        break;
                    case "description":
                        cmp = v1.getDescription().compareToIgnoreCase(v2.getDescription());
                        break;
                    case "severity":
                        cmp = v1.getSeverity().compareToIgnoreCase(v2.getSeverity());
                        break;
                    case "status":
                        Boolean r1 = v1.getResolved();
                        Boolean r2 = v2.getResolved();
                        cmp = (r1 ? "Устранено" : "Не устранено").compareToIgnoreCase(r2 ? "Устранено" : "Не устранено");
                        break;
                    default:
                        cmp = 0;
                }
                return ascending ? cmp : -cmp;
            })
            .toList();
    }

    @Transactional
    public Violation createViolation(Violation violation) {
        UUID inspectionId = violation.getInspection() != null ? violation.getInspection().getId() : null;
        if (inspectionId == null) {
            throw new IllegalArgumentException("Inspection is required");
        }

        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new IllegalArgumentException("Inspection not found: " + inspectionId));
        violation.setInspection(inspection);

        return violationRepository.save(violation);
    }

    @Transactional
    public Violation updateViolation(UUID id, Violation violationDetails) {
        Violation violation = violationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Violation not found: " + id));

        if (violationDetails.getInspection() != null && violationDetails.getInspection().getId() != null) {
            Inspection inspection = inspectionRepository.findById(violationDetails.getInspection().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Inspection not found: " + violationDetails.getInspection().getId()));
            violation.setInspection(inspection);
        }

        if (violationDetails.getDescription() != null) {
            violation.setDescription(violationDetails.getDescription());
        }

        if (violationDetails.getSeverity() != null) {
            violation.setSeverity(violationDetails.getSeverity());
        }

        if (violationDetails.getResolved() != null) {
            boolean newResolved = violationDetails.getResolved();
            if (newResolved && (violation.getResolved() == null || !violation.getResolved())) {
                violation.setCorrectionDeadline(LocalDate.now());
            } else if (!newResolved) {
                violation.setCorrectionDeadline(null);
            }
            violation.setResolved(newResolved);
        }

        return violationRepository.save(violation);
    }

    @Transactional
    public void deleteViolation(UUID id) {
        if (!violationRepository.existsById(id)) {
            throw new RuntimeException("Violation not found: " + id);
        }
        violationRepository.deleteById(id);
    }

    public java.util.Optional<Violation> getViolationById(UUID id) {
        return violationRepository.findById(id);
    }
}
