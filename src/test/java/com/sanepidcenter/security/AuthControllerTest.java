package com.sanepidcenter.security;

import com.sanepidcenter.dto.LoginRequest;
import com.sanepidcenter.dto.RegisterRequest;
import com.sanepidcenter.model.Profile;
import com.sanepidcenter.repository.ProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    @Mock AuthenticationManager authenticationManager;
    @Mock JwtTokenUtil jwtTokenUtil;
    @Mock CustomUserDetailsService userDetailsService;
    @Mock ProfileRepository profileRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks AuthController authController;

    @Test
    void register_ShouldHandleDuplicateAndSuccess() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("u"); req.setPassword("p"); req.setFullName("F"); req.setRole("admin");

        when(profileRepository.existsByUsername("u")).thenReturn(true);
        ResponseEntity<?> bad = authController.register(req);
        assertEquals(400, bad.getStatusCode().value());

        when(profileRepository.existsByUsername("u")).thenReturn(false);
        when(passwordEncoder.encode("p")).thenReturn("enc");
        when(jwtTokenUtil.normalizeRole("admin")).thenReturn("ROLE_ADMIN");
        ResponseEntity<?> ok = authController.register(req);
        assertEquals(200, ok.getStatusCode().value());
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void loginAndMe_ShouldReturnBadRequestWhenAuthFails_And401WhenNoUser() {
        LoginRequest req = new LoginRequest(); req.setUsername("u"); req.setPassword("p");
        doThrow(new RuntimeException("bad")).when(authenticationManager).authenticate(any());
        ResponseEntity<?> bad = authController.login(req);
        assertEquals(400, bad.getStatusCode().value());

        var me = authController.getCurrentUser();
        assertEquals(401, me.getStatusCode().value());

    }
}
