package com.sanepidcenter.controller;

import com.sanepidcenter.model.Inspection;
import com.sanepidcenter.model.Profile;
import com.sanepidcenter.repository.InspectionTypeRepository;
import com.sanepidcenter.repository.ProfileRepository;
import com.sanepidcenter.service.InspectionService;
import com.sanepidcenter.service.OrganizationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InspectionControllerTest {
    @Mock private InspectionService inspectionService;
    @Mock private OrganizationService organizationService;
    @Mock private InspectionTypeRepository inspectionTypeRepository;
    @Mock private ProfileRepository profileRepository;
    @InjectMocks private InspectionController inspectionController;

    @Test
    void listInspections_UsesSearchSortAndModelDefaults() {
        ExtendedModelMap model = new ExtendedModelMap();
        when(inspectionService.searchAllFields("z")).thenReturn(List.of(new Inspection()));
        when(inspectionService.sortInspections(anyList(), eq("status"), eq("desc"))).thenReturn(List.of(new Inspection()));

        String view = inspectionController.listInspections("z", "status", "desc", model);

        assertEquals("inspections/list", view);
        assertEquals("z", model.get("query"));
    }

    @Test
    void viewAndEdit_ShouldHandlePresentAndMissing() {
        UUID id = UUID.randomUUID();
        ExtendedModelMap model = new ExtendedModelMap();
        Inspection inspection = new Inspection();
        inspection.setStartDate(LocalDate.now());
        when(inspectionService.getInspectionById(id)).thenReturn(Optional.of(inspection));
        when(organizationService.getAllOrganizations()).thenReturn(List.of());
        when(inspectionTypeRepository.findAll()).thenReturn(List.of());
        when(profileRepository.findAll()).thenReturn(List.of(Profile.builder().role("ROLE_INSPECTOR").build()));

        assertEquals("inspections/view", inspectionController.viewInspection(id, model));
        assertEquals("inspections/form", inspectionController.editInspectionForm(id, model));

        when(inspectionService.getInspectionById(id)).thenReturn(Optional.empty());
        assertEquals("redirect:/inspections", inspectionController.editInspectionForm(id, model));
    }

    @Test
    void createUpdateDeletePlanned_ShouldReturnRedirects() {
        UUID id = UUID.randomUUID();
        doThrow(new RuntimeException("x")).when(inspectionService).createInspection(any());
        doThrow(new RuntimeException("x")).when(inspectionService).updateInspection(eq(id), any());

        assertEquals("redirect:/inspections/new?error=save_failed", inspectionController.createInspection(new Inspection()));
        assertEquals("redirect:/inspections/" + id + "/edit?error=save_failed", inspectionController.updateInspection(id, new Inspection()));

        doNothing().when(inspectionService).deleteInspection(id);
        assertEquals("redirect:/inspections", inspectionController.deleteInspection(id));

        when(inspectionService.getPlannedInspections()).thenReturn(List.of(new Inspection()));
        assertEquals("inspections/list", inspectionController.listPlannedInspections(new ExtendedModelMap()));
    }
}
