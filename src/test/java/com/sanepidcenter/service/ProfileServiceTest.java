package com.sanepidcenter.service;

import com.sanepidcenter.dto.ProfileDto;
import com.sanepidcenter.model.Profile;
import com.sanepidcenter.repository.ProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ProfileService profileService;

    @Test
    void createProfile_ShouldEncodePasswordAndPersist() {
        ProfileDto dto = new ProfileDto();
        dto.setUsername("new_admin");
        dto.setFullName("New Admin");
        dto.setPhone("+79990000000");
        dto.setPosition("Admin");
        dto.setRole("ROLE_ADMIN");
        dto.setIsActive(true);

        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");
        when(profileRepository.save(any(Profile.class))).thenAnswer(i -> i.getArgument(0));

        ProfileDto created = profileService.createProfile(dto, "secret");

        assertEquals("new_admin", created.getUsername());
        verify(passwordEncoder).encode("secret");
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void updateProfile_WhenExists_ShouldUpdateFields() {
        UUID id = UUID.randomUUID();
        Profile existing = Profile.builder()
                .id(id)
                .username("user")
                .password("pwd")
                .fullName("Old Name")
                .phone("+79990000000")
                .position("Old")
                .role("ROLE_INSPECTOR")
                .isActive(true)
                .build();

        ProfileDto updateDto = new ProfileDto();
        updateDto.setFullName("Updated Name");
        updateDto.setPhone("+78880000000");
        updateDto.setPosition("Lead");
        updateDto.setRole("ROLE_ADMIN");
        updateDto.setIsActive(false);

        when(profileRepository.findById(id)).thenReturn(Optional.of(existing));
        when(profileRepository.save(any(Profile.class))).thenAnswer(i -> i.getArgument(0));

        ProfileDto updated = profileService.updateProfile(id, updateDto);

        assertEquals("Updated Name", updated.getFullName());
        assertEquals("ROLE_ADMIN", updated.getRole());
        assertFalse(updated.getIsActive());
    }
}
