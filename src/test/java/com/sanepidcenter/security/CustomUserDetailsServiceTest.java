package com.sanepidcenter.security;

import com.sanepidcenter.model.Profile;
import com.sanepidcenter.repository.ProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    void loadUserByUsername_WhenActiveAndRoleWithoutPrefix_ShouldAddPrefix() {
        Profile p = Profile.builder().username("u").password("p").role("admin").isActive(true).build();
        when(profileRepository.findByUsername("u")).thenReturn(Optional.of(p));

        var user = service.loadUserByUsername("u");

        assertEquals("u", user.getUsername());
        assertTrue(user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_admin")));
    }

    @Test
    void loadUserByUsername_WhenMissingOrInactive_ShouldThrow() {
        when(profileRepository.findByUsername("x")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("x"));

        Profile inactive = Profile.builder().username("i").password("p").role("ROLE_USER").isActive(false).build();
        when(profileRepository.findByUsername("i")).thenReturn(Optional.of(inactive));
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("i"));
    }
}
