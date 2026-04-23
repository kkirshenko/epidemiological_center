package com.sanepidcenter.service;

import com.sanepidcenter.model.Organization;
import com.sanepidcenter.model.OrganizationType;
import com.sanepidcenter.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrganizationService.
 */
@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private OrganizationService organizationService;

    private Organization testOrganization;
    private OrganizationType testType;

    @BeforeEach
    void setUp() {
        testType = new OrganizationType();
        testType.setId(1);
        testType.setName("Test Type");

        testOrganization = new Organization();
        testOrganization.setId(UUID.randomUUID().toString());
        testOrganization.setName("Test Organization");
        testOrganization.setShortName("TO");
        testOrganization.setCity("Moscow");
        testOrganization.setType(testType);
        testOrganization.setRiskCategory("medium");
        testOrganization.setIsActive(true);
    }

    @Test
    void getAllOrganizations_ShouldReturnList() {
        // Given
        List<Organization> organizations = Arrays.asList(testOrganization);
        when(organizationRepository.findAll()).thenReturn(organizations);

        // When
        List<Organization> result = organizationService.getAllOrganizations();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Organization", result.get(0).getName());
        verify(organizationRepository, times(1)).findAll();
    }

    @Test
    void getOrganizationById_WhenExists_ShouldReturnOrganization() {
        // Given
        String id = testOrganization.getId();
        when(organizationRepository.findByIdWithDetails(id)).thenReturn(Optional.of(testOrganization));

        // When
        Optional<Organization> result = organizationService.getOrganizationById(id);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Test Organization", result.get().getName());
        verify(organizationRepository, times(1)).findByIdWithDetails(id);
    }

    @Test
    void getOrganizationById_WhenNotExists_ShouldReturnEmpty() {
        // Given
        String id = "non-existent-id";
        when(organizationRepository.findByIdWithDetails(id)).thenReturn(Optional.empty());

        // When
        Optional<Organization> result = organizationService.getOrganizationById(id);

        // Then
        assertFalse(result.isPresent());
        verify(organizationRepository, times(1)).findByIdWithDetails(id);
    }

    @Test
    void createOrganization_ShouldSaveAndReturn() {
        // Given
        when(organizationRepository.save(any(Organization.class))).thenReturn(testOrganization);

        // When
        Organization result = organizationService.createOrganization(testOrganization);

        // Then
        assertNotNull(result);
        assertEquals("Test Organization", result.getName());
        verify(organizationRepository, times(1)).save(testOrganization);
    }

    @Test
    void deleteOrganization_ShouldCallRepository() {
        // Given
        String id = testOrganization.getId();

        // When
        organizationService.deleteOrganization(id);

        // Then
        verify(organizationRepository, times(1)).deleteById(id);
    }

    @Test
    void searchByName_ShouldReturnMatchingOrganizations() {
        // Given
        String query = "Test";
        when(organizationRepository.findByNameContainingIgnoreCase(query))
                .thenReturn(Arrays.asList(testOrganization));

        // When
        List<Organization> result = organizationService.searchByName(query);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(organizationRepository, times(1)).findByNameContainingIgnoreCase(query);
    }

    @Test
    void getActiveOrganizations_ShouldReturnOnlyActive() {
        // Given
        when(organizationRepository.findByIsActiveTrue())
                .thenReturn(Arrays.asList(testOrganization));

        // When
        List<Organization> result = organizationService.getActiveOrganizations();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsActive());
        verify(organizationRepository, times(1)).findByIsActiveTrue();
    }
}
