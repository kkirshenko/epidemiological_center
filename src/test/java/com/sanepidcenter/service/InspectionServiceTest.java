package com.sanepidcenter.service;

import com.sanepidcenter.model.Inspection;
import com.sanepidcenter.repository.InspectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InspectionService.
 */
@ExtendWith(MockitoExtension.class)
class InspectionServiceTest {

    @Mock
    private InspectionRepository inspectionRepository;

    @InjectMocks
    private InspectionService inspectionService;

    private Inspection testInspection;

    @BeforeEach
    void setUp() {
        testInspection = new Inspection();
        testInspection.setId(UUID.randomUUID().toString());
        testInspection.setStatus("planned");
        testInspection.setResult("pending");
        testInspection.setScheduledDate(LocalDate.now().plusDays(7));
    }

    @Test
    void getAllInspections_ShouldReturnList() {
        // Given
        List<Inspection> inspections = Arrays.asList(testInspection);
        when(inspectionRepository.findAll()).thenReturn(inspections);

        // When
        List<Inspection> result = inspectionService.getAllInspections();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(inspectionRepository, times(1)).findAll();
    }

    @Test
    void getInspectionById_WhenExists_ShouldReturnInspection() {
        // Given
        String id = testInspection.getId();
        when(inspectionRepository.findByIdWithDetails(id)).thenReturn(Optional.of(testInspection));

        // When
        Optional<Inspection> result = inspectionService.getInspectionById(id);

        // Then
        assertTrue(result.isPresent());
        assertEquals("planned", result.get().getStatus());
        verify(inspectionRepository, times(1)).findByIdWithDetails(id);
    }

    @Test
    void createInspection_ShouldSaveAndReturn() {
        // Given
        when(inspectionRepository.save(any(Inspection.class))).thenReturn(testInspection);

        // When
        Inspection result = inspectionService.createInspection(testInspection);

        // Then
        assertNotNull(result);
        assertEquals("planned", result.getStatus());
        verify(inspectionRepository, times(1)).save(testInspection);
    }

    @Test
    void deleteInspection_ShouldCallRepository() {
        // Given
        String id = testInspection.getId();

        // When
        inspectionService.deleteInspection(id);

        // Then
        verify(inspectionRepository, times(1)).deleteById(id);
    }

    @Test
    void getPlannedInspections_ShouldReturnOnlyPlanned() {
        // Given
        when(inspectionRepository.findByStatusOrderByScheduledDateDesc("planned"))
                .thenReturn(Arrays.asList(testInspection));

        // When
        List<Inspection> result = inspectionService.getPlannedInspections();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("planned", result.get(0).getStatus());
        verify(inspectionRepository, times(1)).findByStatusOrderByScheduledDateDesc("planned");
    }

    @Test
    void getInspectionsByStatus_ShouldReturnMatching() {
        // Given
        String status = "completed";
        when(inspectionRepository.findByStatus(status))
                .thenReturn(Arrays.asList(testInspection));

        // When
        List<Inspection> result = inspectionService.getInspectionsByStatus(status);

        // Then
        assertNotNull(result);
        verify(inspectionRepository, times(1)).findByStatus(status);
    }
}
