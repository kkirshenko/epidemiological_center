package com.sanepidcenter.service;

import com.sanepidcenter.model.Inspection;
import com.sanepidcenter.model.InspectionType;
import com.sanepidcenter.model.Organization;
import com.sanepidcenter.model.Profile;
import com.sanepidcenter.repository.InspectionRepository;
import com.sanepidcenter.repository.InspectionTypeRepository;
import com.sanepidcenter.repository.OrganizationRepository;
import com.sanepidcenter.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InspectionService {

    private final InspectionRepository inspectionRepository;
    private final OrganizationRepository organizationRepository;
    private final InspectionTypeRepository inspectionTypeRepository;
    private final ProfileRepository profileRepository;

    public List<Inspection> getAllInspections() {
        return inspectionRepository.findAll();
    }

    public List<Inspection> getInspectionsByOrganization(UUID organizationId) {
        return inspectionRepository.findByOrganizationId(organizationId);
    }

    public List<Inspection> getInspectionsByInspector(UUID inspectorId) {
        return inspectionRepository.findByInspectorId(inspectorId);
    }

    public List<Inspection> getInspectionsByStatus(String status) {
        return inspectionRepository.findByStatus(status);
    }

    public List<Inspection> getPlannedInspections() {
        return inspectionRepository.findByStatusOrderByScheduledDateDesc("planned");
    }

    public Optional<Inspection> getInspectionById(UUID id) {
        return inspectionRepository.findByIdWithDetails(id);
    }

    public List<Inspection> getInspectionsByDateRange(LocalDate startDate, LocalDate endDate) {
        return inspectionRepository.findByScheduledDateBetween(startDate, endDate);
    }

    public List<Inspection> searchAllFields(String query) {
        return inspectionRepository.findAllFieldsContainingIgnoreCase(query);
    }

    public List<Inspection> sortInspections(List<Inspection> inspections, String sortBy, String sortDir) {
        if (sortBy == null || sortBy.isEmpty()) {
            return inspections;
        }
        
        boolean ascending = sortDir == null || "asc".equalsIgnoreCase(sortDir);
        
        return inspections.stream()
            .sorted((i1, i2) -> {
                int cmp = 0;
                switch (sortBy.toLowerCase()) {
                    case "organization":
                        String org1 = i1.getOrganization() != null ? i1.getOrganization().getName() : "";
                        String org2 = i2.getOrganization() != null ? i2.getOrganization().getName() : "";
                        cmp = org1.compareToIgnoreCase(org2);
                        break;
                    case "type":
                        String type1 = i1.getType() != null ? i1.getType().getName() : "";
                        String type2 = i2.getType() != null ? i2.getType().getName() : "";
                        cmp = type1.compareToIgnoreCase(type2);
                        break;
                    case "scheduleddate":
                        cmp = i1.getScheduledDate().compareTo(i2.getScheduledDate());
                        break;
                    case "status":
                        cmp = i1.getStatus().compareToIgnoreCase(i2.getStatus());
                        break;
                    case "result":
                        cmp = i1.getResult().compareToIgnoreCase(i2.getResult());
                        break;
                    default:
                        cmp = 0;
                }
                return ascending ? cmp : -cmp;
            })
            .toList();
    }

    @Transactional
    public Inspection createInspection(Inspection inspection) {
        if (inspection.getId() == null) {
            inspection.setId(UUID.randomUUID());
        }
        normalizeResultAndStatus(inspection);
        applyInspectionDates(inspection);
        attachManagedReferences(inspection);
        return inspectionRepository.save(inspection);
    }

    @Transactional
    public Inspection updateInspection(UUID id, Inspection updatedInspection) {
        return inspectionRepository.findById(id)
                .map(existing -> {
                    normalizeResultAndStatus(updatedInspection);
                    
                    existing.setScheduledDate(updatedInspection.getScheduledDate());
                    existing.setStatus(updatedInspection.getStatus());
                    existing.setResult(updatedInspection.getResult());
                    existing.setFindingsSummary(updatedInspection.getFindingsSummary());
                    existing.setRecommendations(updatedInspection.getRecommendations());
                    existing.setActNumber(updatedInspection.getActNumber());

                    // Сохраняем существующие startDate и endDate, если они уже установлены
                    if (existing.getStartDate() == null) {
                        existing.setStartDate(updatedInspection.getScheduledDate());
                    }
                    if ("completed".equalsIgnoreCase(updatedInspection.getStatus())) {
                        if (existing.getEndDate() == null) {
                            existing.setEndDate(LocalDate.now());
                        }
                    } else {
                        existing.setEndDate(null);
                    }

                    if (updatedInspection.getOrganization() != null && updatedInspection.getOrganization().getId() != null) {
                        Organization organization = organizationRepository.findById(updatedInspection.getOrganization().getId())
                                .orElseThrow(() -> new IllegalArgumentException("Organization not found: " + updatedInspection.getOrganization().getId()));
                        existing.setOrganization(organization);
                    }
                    if (updatedInspection.getType() != null && updatedInspection.getType().getId() != null) {
                        InspectionType inspectionType = inspectionTypeRepository.findById(updatedInspection.getType().getId())
                                .orElseThrow(() -> new IllegalArgumentException("Inspection type not found: " + updatedInspection.getType().getId()));
                        existing.setType(inspectionType);
                    }
                    if (updatedInspection.getInspector() != null && updatedInspection.getInspector().getId() != null) {
                        Profile inspector = profileRepository.findById(updatedInspection.getInspector().getId())
                                .orElseThrow(() -> new IllegalArgumentException("Inspector not found: " + updatedInspection.getInspector().getId()));
                        existing.setInspector(inspector);
                    }

                    return inspectionRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Inspection not found with id: " + id));
    }

    @Transactional
    public void deleteInspection(UUID id) {
        inspectionRepository.deleteById(id);
    }

    private void attachManagedReferences(Inspection inspection) {
        UUID organizationId = inspection.getOrganization() != null ? inspection.getOrganization().getId() : null;
        Integer typeId = inspection.getType() != null ? inspection.getType().getId() : null;
        UUID inspectorId = inspection.getInspector() != null ? inspection.getInspector().getId() : null;

        if (organizationId == null || typeId == null || inspectorId == null) {
            throw new IllegalArgumentException("Organization, inspection type and inspector are required");
        }

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found: " + organizationId));
        InspectionType inspectionType = inspectionTypeRepository.findById(typeId)
                .orElseThrow(() -> new IllegalArgumentException("Inspection type not found: " + typeId));
        Profile inspector = profileRepository.findById(inspectorId)
                .orElseThrow(() -> new IllegalArgumentException("Inspector not found: " + inspectorId));

        inspection.setOrganization(organization);
        inspection.setType(inspectionType);
        inspection.setInspector(inspector);
    }

    private void normalizeResultAndStatus(Inspection inspection) {
        String result = inspection.getResult();
        String status = inspection.getStatus();

        if ("pending".equalsIgnoreCase(result)) {
            if ("completed".equalsIgnoreCase(status)) {
                inspection.setStatus("planned");
            }
            return;
        }

        if (result != null && !result.isBlank()) {
            inspection.setStatus("completed");
        }
    }

    private void applyInspectionDates(Inspection inspection) {
        if (inspection.getScheduledDate() != null && inspection.getStartDate() == null) {
            inspection.setStartDate(inspection.getScheduledDate());
        }

        if ("completed".equalsIgnoreCase(inspection.getStatus())) {
            if (inspection.getEndDate() == null) {
                inspection.setEndDate(LocalDate.now());
            }
            return;
        }
        inspection.setEndDate(null);
    }
}
