package com.sanepidcenter.config;

import com.sanepidcenter.model.Profile;
import com.sanepidcenter.repository.InspectionTypeRepository;
import com.sanepidcenter.repository.OrganizationTypeRepository;
import com.sanepidcenter.repository.ProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {
    @Mock OrganizationTypeRepository organizationTypeRepository;
    @Mock InspectionTypeRepository inspectionTypeRepository;
    @Mock ProfileRepository profileRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks DataInitializer initializer;

    @Test
    void seedInitialData_WhenEmpty_ShouldInsertReferenceDataAndAdmin() throws Exception {
        when(organizationTypeRepository.count()).thenReturn(0L);
        when(inspectionTypeRepository.count()).thenReturn(0L);
        when(profileRepository.findByUsername("admin1")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("admin1")).thenReturn("enc");

        CommandLineRunner runner = initializer.seedInitialData();
        runner.run();

        verify(organizationTypeRepository).saveAll(any());
        verify(inspectionTypeRepository).saveAll(any());
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void seedInitialData_WhenAdminNeedsUpdate_ShouldSaveAdminOnly() throws Exception {
        Profile admin = Profile.builder().username("admin1").role("ADMIN").isActive(false).build();
        when(organizationTypeRepository.count()).thenReturn(1L);
        when(inspectionTypeRepository.count()).thenReturn(1L);
        when(profileRepository.findByUsername("admin1")).thenReturn(Optional.of(admin));

        initializer.seedInitialData().run();

        verify(profileRepository).save(admin);
        verify(organizationTypeRepository, never()).saveAll(any());
        verify(inspectionTypeRepository, never()).saveAll(any());
    }

    @Test
    void seedInitialData_WhenAdminAlreadyValid_ShouldNotSaveAdmin() throws Exception {
        Profile admin = Profile.builder().username("admin1").role("ROLE_ADMIN").isActive(true).build();
        when(organizationTypeRepository.count()).thenReturn(1L);
        when(inspectionTypeRepository.count()).thenReturn(1L);
        when(profileRepository.findByUsername("admin1")).thenReturn(Optional.of(admin));

        initializer.seedInitialData().run();

        verify(profileRepository, never()).save(any(Profile.class));
        verify(organizationTypeRepository, never()).saveAll(any());
        verify(inspectionTypeRepository, never()).saveAll(any());
    }


    @Test
    void seedInitialData_WhenAdminRoleInvalid_ShouldUpdateRoleAndSave() throws Exception {
        Profile admin = Profile.builder().username("admin1").role("ADMIN").isActive(true).build();
        when(organizationTypeRepository.count()).thenReturn(1L);
        when(inspectionTypeRepository.count()).thenReturn(1L);
        when(profileRepository.findByUsername("admin1")).thenReturn(Optional.of(admin));

        initializer.seedInitialData().run();

        verify(profileRepository).save(admin);
        assertEquals("ROLE_ADMIN", admin.getRole());
        assertTrue(admin.getIsActive());
    }

    @Test
    void seedInitialData_WhenAdminInactiveOrNull_ShouldActivateAndSave() throws Exception {
        Profile admin = Profile.builder().username("admin1").role("ROLE_ADMIN").isActive(null).build();
        when(organizationTypeRepository.count()).thenReturn(1L);
        when(inspectionTypeRepository.count()).thenReturn(1L);
        when(profileRepository.findByUsername("admin1")).thenReturn(Optional.of(admin));

        initializer.seedInitialData().run();

        verify(profileRepository).save(admin);
        assertTrue(admin.getIsActive());
        assertEquals("ROLE_ADMIN", admin.getRole());
    }

}
