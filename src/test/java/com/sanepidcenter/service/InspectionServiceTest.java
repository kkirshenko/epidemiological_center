package com.sanepidcenter.service;

import com.sanepidcenter.model.Inspection;
import com.sanepidcenter.model.InspectionType;
import com.sanepidcenter.model.Organization;
import com.sanepidcenter.model.Profile;
import com.sanepidcenter.repository.InspectionRepository;
import com.sanepidcenter.repository.InspectionTypeRepository;
import com.sanepidcenter.repository.OrganizationRepository;
import com.sanepidcenter.repository.ProfileRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InspectionServiceTest {

    @Mock
    private InspectionRepository inspectionRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private InspectionTypeRepository inspectionTypeRepository;
    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private InspectionService inspectionService;

    private Inspection testInspection;
    private Organization organization;
    private InspectionType inspectionType;
    private Profile inspector;

    @BeforeEach
    void setUp() {
        organization = Organization.builder()
                .id(UUID.randomUUID())
                .name("Org")
                .shortName("O")
                .address("Addr")
                .city("Moscow")
                .directorName("Dir")
                .phone("+79990000000")
                .email("org@test.local")
                .employeeCount(10)
                .riskCategory("medium")
                .notes("notes")
                .build();

        inspectionType = new InspectionType();
        inspectionType.setId(1);
        inspectionType.setName("Plan");

        inspector = Profile.builder()
                .id(UUID.randomUUID())
                .username("insp")
                .password("pwd")
                .fullName("Inspector")
                .role("ROLE_INSPECTOR")
                .phone("+79990000000")
                .position("Inspector")
                .isActive(true)
                .build();

        testInspection = Inspection.builder()
                .id(UUID.randomUUID())
                .organization(organization)
                .type(inspectionType)
                .inspector(inspector)
                .scheduledDate(LocalDate.now().plusDays(7))
                .status("planned")
                .result("pending")
                .findingsSummary("none")
                .recommendations("none")
                .build();
    }

    @Test
    void getAllInspections_ShouldReturnList() {
        when(inspectionRepository.findAllWithDetails()).thenReturn(List.of(testInspection));

        List<Inspection> result = inspectionService.getAllInspections();

        assertEquals(1, result.size());
        verify(inspectionRepository).findAllWithDetails();
    }

    @Test
    void getInspectionById_WhenExists_ShouldReturnInspection() {
        UUID id = testInspection.getId();
        when(inspectionRepository.findByIdWithDetails(id)).thenReturn(Optional.of(testInspection));

        Optional<Inspection> result = inspectionService.getInspectionById(id);

        assertTrue(result.isPresent());
        assertEquals("planned", result.get().getStatus());
    }

    @Test
    void createInspection_ShouldAttachReferencesAndSave() {
        when(organizationRepository.findById(organization.getId())).thenReturn(Optional.of(organization));
        when(inspectionTypeRepository.findById(inspectionType.getId())).thenReturn(Optional.of(inspectionType));
        when(profileRepository.findById(inspector.getId())).thenReturn(Optional.of(inspector));
        when(inspectionRepository.save(any(Inspection.class))).thenReturn(testInspection);

        Inspection result = inspectionService.createInspection(testInspection);

        assertNotNull(result);
        verify(inspectionRepository).save(testInspection);
    }

    @Test
    void createInspection_Completed_ShouldSetEndDate() {
        testInspection.setStatus("completed");
        testInspection.setResult("satisfactory");

        when(organizationRepository.findById(organization.getId())).thenReturn(Optional.of(organization));
        when(inspectionTypeRepository.findById(inspectionType.getId())).thenReturn(Optional.of(inspectionType));
        when(profileRepository.findById(inspector.getId())).thenReturn(Optional.of(inspector));
        when(inspectionRepository.save(any(Inspection.class))).thenAnswer(i -> i.getArgument(0));

        Inspection result = inspectionService.createInspection(testInspection);

        assertEquals("completed", result.getStatus());
        assertNotNull(result.getEndDate());
    }

    @Test
    void getPlannedInspections_ShouldReturnOnlyPlanned() {
        when(inspectionRepository.findByStatusOrderByScheduledDateDesc("planned")).thenReturn(List.of(testInspection));

        List<Inspection> result = inspectionService.getPlannedInspections();

        assertEquals(1, result.size());
        assertEquals("planned", result.get(0).getStatus());
    }
}
