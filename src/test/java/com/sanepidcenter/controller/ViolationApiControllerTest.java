package com.sanepidcenter.controller;

import com.sanepidcenter.model.Violation;
import com.sanepidcenter.service.ViolationService;
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
class ViolationApiControllerTest {
    @Mock ViolationService violationService;
    @InjectMocks ViolationApiController controller;

    @Test
    void apiCrud_ShouldCoverOkBadRequestNotFound() {
        UUID id = UUID.randomUUID();
        Violation v = new Violation();
        v.setId(id);

        when(violationService.getAllViolations()).thenReturn(List.of(v));
        assertEquals(200, controller.getAllViolations().getStatusCode().value());

        when(violationService.getViolationById(id)).thenReturn(Optional.of(v));
        assertEquals(200, controller.getViolation(id).getStatusCode().value());
        when(violationService.getViolationById(id)).thenReturn(Optional.empty());
        assertEquals(404, controller.getViolation(id).getStatusCode().value());

        when(violationService.createViolation(v)).thenReturn(v);
        assertEquals(200, controller.createViolation(v).getStatusCode().value());
        doThrow(new IllegalArgumentException("bad")).when(violationService).createViolation(v);
        assertEquals(400, controller.createViolation(v).getStatusCode().value());

        when(violationService.updateViolation(id, v)).thenReturn(v);
        assertEquals(200, controller.updateViolation(id, v).getStatusCode().value());
        doThrow(new IllegalArgumentException("bad")).when(violationService).updateViolation(id, v);
        assertEquals(400, controller.updateViolation(id, v).getStatusCode().value());
        doThrow(new RuntimeException("nf")).when(violationService).updateViolation(id, v);
        assertEquals(404, controller.updateViolation(id, v).getStatusCode().value());

        assertEquals(204, controller.deleteViolation(id).getStatusCode().value());
    }
}
