package com.sanepidcenter.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticate_WhenAuthorizationHeaderContainsRawTokenWithoutBearer() throws Exception {
        JwtTokenUtil tokenUtil = mock(JwtTokenUtil.class);
        CustomUserDetailsService userDetailsService = mock(CustomUserDetailsService.class);
        TestableJwtAuthenticationFilter filter = new TestableJwtAuthenticationFilter(tokenUtil, userDetailsService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "raw.jwt.token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        UserDetails userDetails = new User("admin1", "pwd", List.of());

        when(tokenUtil.validateToken("raw.jwt.token")).thenReturn(true);
        when(tokenUtil.extractUsername("raw.jwt.token")).thenReturn("admin1");
        when(userDetailsService.loadUserByUsername("admin1")).thenReturn(userDetails);
        when(tokenUtil.validateToken("raw.jwt.token", userDetails)).thenReturn(true);
        when(tokenUtil.extractRoles("raw.jwt.token")).thenReturn(List.of("admin"));
        when(tokenUtil.normalizeRole("admin")).thenReturn("ROLE_ADMIN");

        filter.doFilterPublic(request, response, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())));
        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void shouldAuthenticate_WhenTokenComesFromXAuthTokenHeader() throws Exception {
        JwtTokenUtil tokenUtil = mock(JwtTokenUtil.class);
        CustomUserDetailsService userDetailsService = mock(CustomUserDetailsService.class);
        TestableJwtAuthenticationFilter filter = new TestableJwtAuthenticationFilter(tokenUtil, userDetailsService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Auth-Token", "custom.token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        UserDetails userDetails = new User("inspector1", "pwd", List.of());

        when(tokenUtil.validateToken("custom.token")).thenReturn(true);
        when(tokenUtil.extractUsername("custom.token")).thenReturn("inspector1");
        when(userDetailsService.loadUserByUsername("inspector1")).thenReturn(userDetails);
        when(tokenUtil.validateToken("custom.token", userDetails)).thenReturn(true);
        when(tokenUtil.extractRoles("custom.token")).thenReturn(List.of("ROLE_INSPECTOR"));
        when(tokenUtil.normalizeRole("ROLE_INSPECTOR")).thenReturn("ROLE_INSPECTOR");

        filter.doFilterPublic(request, response, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> "ROLE_INSPECTOR".equals(a.getAuthority())));
        verify(chain, times(1)).doFilter(request, response);
    }

    private static class TestableJwtAuthenticationFilter extends JwtAuthenticationFilter {
        TestableJwtAuthenticationFilter(JwtTokenUtil tokenUtil, CustomUserDetailsService userDetailsService) {
            super(tokenUtil, userDetailsService);
        }

        void doFilterPublic(MockHttpServletRequest request, MockHttpServletResponse response, FilterChain chain) throws Exception {
            super.doFilterInternal(request, response, chain);
        }
    }
}
