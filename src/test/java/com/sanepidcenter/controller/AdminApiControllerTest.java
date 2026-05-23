package com.sanepidcenter.controller;

import com.sanepidcenter.dto.AdminUserCreateRequest;
import com.sanepidcenter.dto.AdminUserUpdateRequest;
import com.sanepidcenter.dto.ProfileDto;
import com.sanepidcenter.repository.InspectionRepository;
import com.sanepidcenter.repository.OrganizationRepository;
import com.sanepidcenter.repository.ProfileRepository;
import com.sanepidcenter.repository.ViolationRepository;
import com.sanepidcenter.security.JwtTokenUtil;
import com.sanepidcenter.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminApiControllerTest {
    @Mock ProfileService profileService;
    @Mock ProfileRepository profileRepository;
    @Mock OrganizationRepository organizationRepository;
    @Mock InspectionRepository inspectionRepository;
    @Mock ViolationRepository violationRepository;
    @Mock JwtTokenUtil jwtTokenUtil;
    @InjectMocks AdminApiController controller;

    @Test
    void usersCrudAndMetrics() {
        UUID id = UUID.randomUUID();
        when(profileService.getAllProfiles()).thenReturn(List.of(new ProfileDto()));
        assertEquals(200, controller.getAllUsers().getStatusCode().value());

        when(profileService.getProfileById(id)).thenReturn(null);
        assertEquals(404, controller.getUserById(id).getStatusCode().value());

        when(profileService.getProfileById(id)).thenReturn(new ProfileDto());
        assertEquals(200, controller.getUserById(id).getStatusCode().value());

        AdminUserCreateRequest create = new AdminUserCreateRequest();
        create.setUsername("u"); create.setPassword("p"); create.setRole("admin");
        when(profileRepository.existsByUsername("u")).thenReturn(true);
        assertEquals(400, controller.createUser(create).getStatusCode().value());

        when(profileRepository.existsByUsername("u")).thenReturn(false);
        when(jwtTokenUtil.normalizeRole("admin")).thenReturn("ROLE_ADMIN");
        when(profileService.createProfile(any(ProfileDto.class), eq("p"))).thenReturn(new ProfileDto());
        assertEquals(200, controller.createUser(create).getStatusCode().value());

        AdminUserUpdateRequest upd = new AdminUserUpdateRequest();
        upd.setRole("admin"); upd.setIsActive(true);
        when(jwtTokenUtil.normalizeRole("admin")).thenReturn("ROLE_ADMIN");
        when(profileService.updateProfile(eq(id), any(ProfileDto.class))).thenReturn(new ProfileDto());
        assertEquals(200, controller.updateUser(id, upd).getStatusCode().value());

        doThrow(new RuntimeException()).when(profileService).updateProfile(eq(id), any(ProfileDto.class));
        assertEquals(404, controller.updateUser(id, upd).getStatusCode().value());

        when(profileService.getProfileById(id)).thenReturn(new ProfileDto());
        when(profileService.updateProfile(eq(id), any(ProfileDto.class))).thenReturn(new ProfileDto());
        assertEquals(200, controller.setUserStatus(id, false).getStatusCode().value());

        when(profileService.getProfileById(id)).thenReturn(null);
        assertEquals(404, controller.setUserStatus(id, true).getStatusCode().value());

        when(profileService.getProfileById(id)).thenReturn(null);
        assertEquals(404, controller.deleteUser(id).getStatusCode().value());
        when(profileService.getProfileById(id)).thenReturn(new ProfileDto());
        assertEquals(204, controller.deleteUser(id).getStatusCode().value());

        when(profileRepository.count()).thenReturn(1L);
        when(organizationRepository.count()).thenReturn(2L);
        when(inspectionRepository.count()).thenReturn(3L);
        when(violationRepository.count()).thenReturn(4L);
        assertEquals(200, controller.getMetrics().getStatusCode().value());
    }
}
