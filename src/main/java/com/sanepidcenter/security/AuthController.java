package com.sanepidcenter.security;

import com.sanepidcenter.dto.AuthResponse;
import com.sanepidcenter.dto.LoginRequest;
import com.sanepidcenter.dto.RegisterRequest;
import com.sanepidcenter.model.Profile;
import com.sanepidcenter.repository.ProfileRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST API controller for authentication operations.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API for user authentication and registration")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final CustomUserDetailsService userDetailsService;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user and return JWT token")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            Profile profile = profileRepository.findByUsername(request.getUsername()).orElseThrow();
            
            String token = jwtTokenUtil.generateToken(request.getUsername(), profile.getRole());
            
            AuthResponse response = AuthResponse.builder()
                .token(token)
                .username(profile.getUsername())
                .role(profile.getRole())
                .fullName(profile.getFullName())
                .build();
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid username or password");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Create a new user account")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (profileRepository.existsByUsername(request.getUsername())) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Username already exists");
            return ResponseEntity.badRequest().body(error);
        }

        Profile profile = Profile.builder()
            .id(UUID.randomUUID())
            .username(request.getUsername())
            .password(passwordEncoder.encode(request.getPassword()))
            .fullName(request.getFullName())
            .phone(request.getPhone())
            .position(request.getPosition())
            .role(request.getRole() != null ? request.getRole() : "ROLE_INSPECTOR")
            .isActive(true)
            .build();

        profileRepository.save(profile);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User registered successfully");
        response.put("username", profile.getUsername());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user info", description = "Return information about currently authenticated user")
    public ResponseEntity<?> getCurrentUser() {
        // This endpoint requires authentication, user details will be available from security context
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Profile profile = profileRepository.findByUsername(username).orElse(null);
            if (profile != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("username", profile.getUsername());
                response.put("fullName", profile.getFullName());
                response.put("role", profile.getRole());
                response.put("position", profile.getPosition());
                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
    }
}
