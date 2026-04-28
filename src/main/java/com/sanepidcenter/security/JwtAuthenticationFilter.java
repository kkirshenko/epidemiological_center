package com.sanepidcenter.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * JWT Authentication Filter for processing JWT tokens in requests.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && jwtTokenUtil.validateToken(jwt)) {
                String username = jwtTokenUtil.extractUsername(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (!jwtTokenUtil.validateToken(jwt, userDetails)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                Collection<? extends GrantedAuthority> authorities = resolveAuthorities(jwt, userDetails);
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken)) {
            String trimmed = bearerToken.trim();
            if (trimmed.startsWith("Bearer ")) {
                return trimmed.substring(7).trim();
            }
            return trimmed;
        }

        String tokenFromCustomHeader = request.getHeader("X-Auth-Token");
        if (StringUtils.hasText(tokenFromCustomHeader)) {
            return tokenFromCustomHeader.trim();
        }
        return null;
    }

    private Collection<? extends GrantedAuthority> resolveAuthorities(String jwt, UserDetails userDetails) {
        List<String> tokenRoles = jwtTokenUtil.extractRoles(jwt);
        if (tokenRoles == null || tokenRoles.isEmpty()) {
            return userDetails.getAuthorities();
        }

        return tokenRoles.stream()
                .map(jwtTokenUtil::normalizeRole)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}
