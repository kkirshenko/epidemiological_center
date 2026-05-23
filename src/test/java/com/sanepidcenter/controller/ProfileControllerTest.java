package com.sanepidcenter.controller;

import com.sanepidcenter.dto.ProfileDto;
import com.sanepidcenter.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    @Mock
    private ProfileService profileService;

    @InjectMocks
    private ProfileController profileController;

    @Test
    void listUsers_ShouldPopulateModelAndReturnView() {
        when(profileService.getAllProfiles()).thenReturn(List.of(new ProfileDto()));
        ExtendedModelMap model = new ExtendedModelMap();

        String view = profileController.listUsers(model);

        assertEquals("users/list", view);
        assertTrue(model.containsAttribute("users"));
    }

    @Test
    void editUserForm_WhenMissing_ShouldRedirect() {
        UUID id = UUID.randomUUID();
        when(profileService.getProfileById(id)).thenReturn(null);

        String view = profileController.editUserForm(id, new ExtendedModelMap());

        assertEquals("redirect:/users", view);
    }

    @Test
    void updateDeleteToggle_ShouldCallServiceAndSetFlash() {
        UUID id = UUID.randomUUID();
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();

        String updateView = profileController.updateUser(id, "F", "P", "Pos", "ROLE_ADMIN", true, attrs);
        String deleteView = profileController.deleteUser(id, attrs);
        String toggleView = profileController.toggleUserStatus(id, attrs);

        assertEquals("redirect:/users", updateView);
        assertEquals("redirect:/users", deleteView);
        assertEquals("redirect:/users", toggleView);
        verify(profileService).updateProfile(eq(id), any(ProfileDto.class));
        verify(profileService).deleteProfile(id);
        verify(profileService).toggleProfileStatus(id);
        assertTrue(attrs.getFlashAttributes().containsKey("successMessage"));
    }
}
