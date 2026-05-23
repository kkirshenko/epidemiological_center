package com.sanepidcenter.config;

import com.sanepidcenter.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

    @Test
    void passwordEncoder_ShouldEncodeAndMatchBcryptAndPlainText() {
        SecurityConfig config = new SecurityConfig(mock(JwtAuthenticationFilter.class));

        PasswordEncoder encoder = config.passwordEncoder();
        String raw = "secret123";

        String encoded = encoder.encode(raw);
        assertNotNull(encoded);
        assertTrue(encoded.startsWith("$2"));
        assertTrue(encoder.matches(raw, encoded));
        assertFalse(encoder.matches("wrong", encoded));

        assertTrue(encoder.matches(raw, raw));
        assertFalse(encoder.matches(raw, "other"));
        assertFalse(encoder.matches(raw, null));
    }

    @Test
    void corsConfigurationSource_ShouldContainExpectedDefaultConfiguration() {
        SecurityConfig config = new SecurityConfig(mock(JwtAuthenticationFilter.class));

        CorsConfigurationSource source = config.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");

        CorsConfiguration cors = source.getCorsConfiguration(request);
        assertNotNull(cors);
        assertEquals(1, cors.getAllowedOrigins().size());
        assertEquals("*", cors.getAllowedOrigins().get(0));
        assertTrue(cors.getAllowedMethods().contains("PATCH"));
        assertEquals("*", cors.getAllowedHeaders().get(0));
        assertTrue(cors.getExposedHeaders().contains("Authorization"));
    }

    @Test
    void authenticationManager_ShouldDelegateToAuthenticationConfiguration() throws Exception {
        SecurityConfig config = new SecurityConfig(mock(JwtAuthenticationFilter.class));
        AuthenticationConfiguration authenticationConfiguration = mock(AuthenticationConfiguration.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);

        AuthenticationManager result = config.authenticationManager(authenticationConfiguration);

        assertSame(authenticationManager, result);
        verify(authenticationConfiguration).getAuthenticationManager();
    }
}
