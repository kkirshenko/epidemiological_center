package com.sanepidcenter.controller;

import com.sanepidcenter.dto.OrganizationDto;
import com.sanepidcenter.dto.OrganizationTypeDto;
import com.sanepidcenter.model.Organization;
import com.sanepidcenter.model.OrganizationType;
import com.sanepidcenter.service.OrganizationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationApiControllerTest {
    @Mock OrganizationService organizationService;
    @InjectMocks OrganizationApiController controller;

    @Test
    void getByIdCreateUpdateDeleteSearch_ShouldCoverBranches() {
        Organization org = new Organization();
        org.setId(UUID.randomUUID());
        org.setName("Org");
        OrganizationType type = new OrganizationType();
        type.setId(1); type.setName("T");
        org.setType(type);

        when(organizationService.getAllOrganizations()).thenReturn(List.of(org));
        assertEquals(200, controller.getAllOrganizations().getStatusCode().value());

        when(organizationService.getOrganizationById(org.getId())).thenReturn(Optional.of(org));
        assertEquals(200, controller.getOrganizationById(org.getId()).getStatusCode().value());
        when(organizationService.getOrganizationById(org.getId())).thenReturn(Optional.empty());
        assertEquals(404, controller.getOrganizationById(org.getId()).getStatusCode().value());

        OrganizationDto dto = new OrganizationDto();
        dto.setName("X");
        OrganizationTypeDto typeDto = new OrganizationTypeDto();
        typeDto.setId(1);
        dto.setType(typeDto);

        when(organizationService.createOrganization(any(Organization.class))).thenReturn(org);
        assertEquals(200, controller.createOrganization(dto).getStatusCode().value());
        doThrow(new IllegalArgumentException("bad")).when(organizationService).createOrganization(any(Organization.class));
        assertEquals(400, controller.createOrganization(dto).getStatusCode().value());

        when(organizationService.updateOrganization(eq(org.getId()), any(Organization.class))).thenReturn(org);
        assertEquals(200, controller.updateOrganization(org.getId(), dto).getStatusCode().value());
        doThrow(new IllegalArgumentException("bad")).when(organizationService).updateOrganization(eq(org.getId()), any(Organization.class));
        assertEquals(400, controller.updateOrganization(org.getId(), dto).getStatusCode().value());

        assertEquals(200, controller.deleteOrganization(org.getId()).getStatusCode().value());

        when(organizationService.searchAllFields("q")).thenReturn(List.of(org));
        assertEquals(200, controller.searchOrganizations("q").getStatusCode().value());
    }
}
