package com.sanepidcenter.service;

import com.sanepidcenter.model.Organization;
import com.sanepidcenter.model.OrganizationType;
import com.sanepidcenter.repository.OrganizationRepository;
import com.sanepidcenter.repository.OrganizationTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationTypeRepository organizationTypeRepository;

    @InjectMocks
    private OrganizationService organizationService;

    private Organization testOrganization;
    private OrganizationType testType;

    @BeforeEach
    void setUp() {
        testType = new OrganizationType();
        testType.setId(1);
        testType.setName("Test Type");

        testOrganization = Organization.builder()
                .id(UUID.randomUUID())
                .name("Test Organization")
                .shortName("TO")
                .type(testType)
                .address("Lenina 1")
                .city("Moscow")
                .directorName("Director Name")
                .phone("+79990000000")
                .email("org@test.local")
                .employeeCount(10)
                .riskCategory("medium")
                .notes("notes")
                .isActive(true)
                .build();
    }

    @Test
    void getAllOrganizations_ShouldReturnList() {
        when(organizationRepository.findAll()).thenReturn(List.of(testOrganization));

        List<Organization> result = organizationService.getAllOrganizations();

        assertEquals(1, result.size());
        verify(organizationRepository).findAll();
    }

    @Test
    void getOrganizationById_WhenExists_ShouldReturnOrganization() {
        UUID id = testOrganization.getId();
        when(organizationRepository.findByIdWithDetails(id)).thenReturn(Optional.of(testOrganization));

        Optional<Organization> result = organizationService.getOrganizationById(id);

        assertTrue(result.isPresent());
        assertEquals(testOrganization.getName(), result.get().getName());
    }

    @Test
    void createOrganization_ShouldResolveTypeAndSave() {
        when(organizationTypeRepository.findById(testType.getId())).thenReturn(Optional.of(testType));
        when(organizationRepository.save(any(Organization.class))).thenReturn(testOrganization);

        Organization result = organizationService.createOrganization(testOrganization);

        assertNotNull(result);
        assertEquals(testType, result.getType());
        verify(organizationTypeRepository).findById(testType.getId());
        verify(organizationRepository).save(testOrganization);
    }

    @Test
    void createOrganization_WithoutType_ShouldThrow() {
        testOrganization.setType(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> organizationService.createOrganization(testOrganization)
        );

        assertTrue(exception.getMessage().contains("type"));
        verify(organizationRepository, never()).save(any());
    }

    @Test
    void deleteOrganization_ShouldCallRepository() {
        UUID id = testOrganization.getId();

        organizationService.deleteOrganization(id);

        verify(organizationRepository).deleteById(id);
    }

    @Test
    void sortOrganizations_ByNameDescending_ShouldSort() {
        Organization second = Organization.builder().id(UUID.randomUUID()).name("Beta").shortName("B").city("Kazan").riskCategory("high").build();
        Organization first = Organization.builder().id(UUID.randomUUID()).name("Alpha").shortName("A").city("Moscow").riskCategory("low").build();

        List<Organization> result = organizationService.sortOrganizations(List.of(first, second), "name", "desc");

        assertEquals("Beta", result.get(0).getName());
    }

    @Test
    void updateOrganization_WhenNotFound_ShouldThrow() {
        UUID id = UUID.randomUUID();
        when(organizationRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> organizationService.updateOrganization(id, testOrganization));

        assertTrue(exception.getMessage().contains("Organization not found"));
    }
}
