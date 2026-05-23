package com.sanepidcenter.controller;

import com.sanepidcenter.model.Organization;
import com.sanepidcenter.repository.OrganizationTypeRepository;
import com.sanepidcenter.service.OrganizationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationControllerTest {

    @Mock
    private OrganizationService organizationService;
    @Mock
    private OrganizationTypeRepository organizationTypeRepository;

    @InjectMocks
    private OrganizationController organizationController;

    @Test
    void listOrganizations_WithQueryAndSort_ShouldUseSearchAndSort() {
        ExtendedModelMap model = new ExtendedModelMap();
        when(organizationService.searchAllFields("abc")).thenReturn(List.of(new Organization()));
        when(organizationService.sortOrganizations(anyList(), eq("name"), eq("desc"))).thenReturn(List.of(new Organization()));

        String view = organizationController.listOrganizations("abc", "name", "desc", model);

        assertEquals("organizations/list", view);
        verify(organizationService).searchAllFields("abc");
        verify(organizationService).sortOrganizations(anyList(), eq("name"), eq("desc"));
    }

    @Test
    void viewAndEditForms_ShouldHandlePresentAndMissing() {
        UUID id = UUID.randomUUID();
        ExtendedModelMap model = new ExtendedModelMap();
        Organization org = new Organization();
        when(organizationService.getOrganizationById(id)).thenReturn(Optional.of(org));
        when(organizationTypeRepository.findAll()).thenReturn(List.of());

        assertEquals("organizations/view", organizationController.viewOrganization(id, model));
        assertEquals("organizations/form", organizationController.editOrganizationForm(id, model));

        when(organizationService.getOrganizationById(id)).thenReturn(Optional.empty());
        assertEquals("redirect:/organizations", organizationController.viewOrganization(id, model));
    }

    @Test
    void createUpdateDeleteAndSearch_ShouldReturnRedirects() {
        UUID id = UUID.randomUUID();
        assertEquals("redirect:/organizations", organizationController.createOrganization(new Organization()));
        assertEquals("redirect:/organizations", organizationController.updateOrganization(id, new Organization()));
        assertEquals("redirect:/organizations", organizationController.deleteOrganization(id));
        assertEquals("redirect:/organizations?query=q", organizationController.searchOrganizations("q", new ExtendedModelMap()));
        verify(organizationService).deleteOrganization(id);
    }
}
