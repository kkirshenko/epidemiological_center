package com.sanepidcenter.service;

import com.sanepidcenter.model.Inspection;
import com.sanepidcenter.model.Organization;
import com.sanepidcenter.model.Violation;
import com.sanepidcenter.repository.InspectionRepository;
import com.sanepidcenter.repository.ViolationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ViolationServiceTest {

    @Mock
    private ViolationRepository violationRepository;
    @Mock
    private InspectionRepository inspectionRepository;

    @InjectMocks
    private ViolationService violationService;

    private Inspection inspection;
    private Violation unresolvedViolation;

    @BeforeEach
    void setUp() {
        Organization organization = Organization.builder()
                .id(UUID.randomUUID())
                .name("Alpha LLC")
                .build();

        inspection = Inspection.builder()
                .id(UUID.randomUUID())
                .organization(organization)
                .build();

        unresolvedViolation = Violation.builder()
                .id(UUID.randomUUID())
                .inspection(inspection)
                .description("No fire extinguisher")
                .severity("high")
                .resolved(false)
                .build();
    }

    @Test
    void sortViolations_ByInspectionAscending_ShouldSortByOrganizationName() {
        Inspection secondInspection = Inspection.builder()
                .id(UUID.randomUUID())
                .organization(Organization.builder().id(UUID.randomUUID()).name("Beta LLC").build())
                .build();

        Violation secondViolation = Violation.builder()
                .id(UUID.randomUUID())
                .inspection(secondInspection)
                .description("Broken lights")
                .severity("low")
                .resolved(false)
                .build();

        List<Violation> result = violationService.sortViolations(List.of(secondViolation, unresolvedViolation), "inspection", "asc");

        assertEquals("Alpha LLC", result.get(0).getInspection().getOrganization().getName());
        assertEquals("Beta LLC", result.get(1).getInspection().getOrganization().getName());
    }

    @Test
    void createViolation_WhenInspectionNotFound_ShouldThrow() {
        when(inspectionRepository.findById(inspection.getId())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> violationService.createViolation(unresolvedViolation));

        assertTrue(exception.getMessage().contains("Inspection not found"));
        verify(violationRepository, never()).save(any(Violation.class));
    }

    @Test
    void updateViolation_MarkResolved_ShouldSetDeadlineAndPersist() {
        UUID violationId = unresolvedViolation.getId();
        Violation patch = new Violation();
        patch.setResolved(true);

        when(violationRepository.findById(violationId)).thenReturn(Optional.of(unresolvedViolation));
        when(violationRepository.save(unresolvedViolation)).thenReturn(unresolvedViolation);

        Violation result = violationService.updateViolation(violationId, patch);

        assertTrue(result.getResolved());
        assertEquals(LocalDate.now(), result.getCorrectionDeadline());
        verify(violationRepository).save(unresolvedViolation);
    }

    @Test
    void deleteViolation_WhenMissing_ShouldThrow() {
        UUID id = UUID.randomUUID();
        when(violationRepository.existsById(id)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> violationService.deleteViolation(id));

        assertTrue(exception.getMessage().contains("Violation not found"));
        verify(violationRepository, never()).deleteById(any());
    }

    @Test
    void createViolation_WhenInspectionExists_ShouldSave() {
        when(inspectionRepository.findById(inspection.getId())).thenReturn(Optional.of(inspection));
        when(violationRepository.save(unresolvedViolation)).thenReturn(unresolvedViolation);

        Violation result = violationService.createViolation(unresolvedViolation);

        assertEquals(inspection, result.getInspection());
        verify(violationRepository).save(unresolvedViolation);
    }

    @Test
    void sortViolations_ByStatusDescending_ShouldSort() {
        Violation resolved = Violation.builder().id(UUID.randomUUID()).inspection(inspection).description("D").severity("low").resolved(true).build();

        List<Violation> result = violationService.sortViolations(List.of(unresolvedViolation, resolved), "status", "desc");

        assertTrue(result.get(0).getResolved());
    }
}
