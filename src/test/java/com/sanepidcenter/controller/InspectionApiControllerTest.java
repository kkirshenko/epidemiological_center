package com.sanepidcenter.controller;

import com.sanepidcenter.model.Inspection;
import com.sanepidcenter.service.InspectionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InspectionApiControllerTest {
    @Mock InspectionService inspectionService;
    @InjectMocks InspectionApiController controller;

    @Test
    void apiCrud_ShouldCoverOkBadRequestNotFound() {
        UUID id = UUID.randomUUID();
        Inspection i = new Inspection();
        i.setId(id);

        when(inspectionService.getAllInspections()).thenReturn(List.of(i));
        assertEquals(200, controller.getAllInspections().getStatusCode().value());

        when(inspectionService.getInspectionById(id)).thenReturn(Optional.of(i));
        assertEquals(200, controller.getInspection(id).getStatusCode().value());
        when(inspectionService.getInspectionById(id)).thenReturn(Optional.empty());
        assertEquals(404, controller.getInspection(id).getStatusCode().value());

        when(inspectionService.createInspection(i)).thenReturn(i);
        assertEquals(200, controller.createInspection(i).getStatusCode().value());
        doThrow(new IllegalArgumentException("bad")).when(inspectionService).createInspection(i);
        assertEquals(400, controller.createInspection(i).getStatusCode().value());

        when(inspectionService.updateInspection(id, i)).thenReturn(i);
        assertEquals(200, controller.updateInspection(id, i).getStatusCode().value());
        doThrow(new IllegalArgumentException("bad")).when(inspectionService).updateInspection(id, i);
        assertEquals(400, controller.updateInspection(id, i).getStatusCode().value());
        doThrow(new RuntimeException("nf")).when(inspectionService).updateInspection(id, i);
        assertEquals(404, controller.updateInspection(id, i).getStatusCode().value());

        assertEquals(204, controller.deleteInspection(id).getStatusCode().value());
    }
}
