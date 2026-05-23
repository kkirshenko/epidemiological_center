package com.sanepidcenter.controller;

import com.sanepidcenter.model.Inspection;
import com.sanepidcenter.model.Violation;
import com.sanepidcenter.repository.InspectionRepository;
import com.sanepidcenter.repository.ViolationRepository;
import com.sanepidcenter.service.InspectionService;
import com.sanepidcenter.service.ViolationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ViolationControllerTest {
    @Mock private ViolationService violationService;
    @Mock private InspectionService inspectionService;
    @Mock private InspectionRepository inspectionRepository;
    @Mock private ViolationRepository violationRepository;
    @InjectMocks private ViolationController violationController;

    @Test
    void listAndView_ShouldPopulateModel() {
        ExtendedModelMap model = new ExtendedModelMap();
        when(violationService.getAllViolations()).thenReturn(List.of(new Violation()));
        when(inspectionService.getAllInspections()).thenReturn(List.of(new Inspection()));

        assertEquals("violations/list", violationController.listViolations(null, null, null, model));

        UUID id = UUID.randomUUID();
        Violation violation = new Violation();
        violation.setResolved(true);
        violation.setCreatedAt(LocalDateTime.now().minusDays(2));
        violation.setCorrectionDeadline(LocalDateTime.now().toLocalDate());
        when(violationService.getViolationById(id)).thenReturn(Optional.of(violation));

        assertEquals("violations/view", violationController.viewViolation(id, model));
    }

    @Test
    void createUpdateDelete_ShouldHandleSuccessAndError() {
        UUID id = UUID.randomUUID();
        UUID inspectionId = UUID.randomUUID();
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();
        Inspection inspection = new Inspection();
        when(inspectionRepository.findById(inspectionId)).thenReturn(Optional.of(inspection));

        assertEquals("redirect:/violations", violationController.createViolation(inspectionId, "d", "high", attrs));
        verify(violationRepository).save(any(Violation.class));

        assertEquals("redirect:/violations", violationController.updateViolation(id, inspectionId, "d", "low", true, attrs));
        verify(violationService).updateViolation(eq(id), any(Violation.class));

        assertEquals("redirect:/violations", violationController.deleteViolation(id, attrs));
        verify(violationService).deleteViolation(id);

        when(inspectionRepository.findById(inspectionId)).thenReturn(Optional.empty());
        assertEquals("redirect:/violations", violationController.createViolation(inspectionId, "d", "high", attrs));
    }
}
