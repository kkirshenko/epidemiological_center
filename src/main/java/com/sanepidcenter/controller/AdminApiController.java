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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Administrative endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AdminApiController {

    private final ProfileService profileService;
    private final ProfileRepository profileRepository;
    private final OrganizationRepository organizationRepository;
    private final InspectionRepository inspectionRepository;
    private final ViolationRepository violationRepository;
    private final JwtTokenUtil jwtTokenUtil;

    @GetMapping("/users")
    @Operation(summary = "Get all users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(profileService.getAllProfiles());
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user by id")
    public ResponseEntity<?> getUserById(@PathVariable UUID id) {
        ProfileDto profile = profileService.getProfileById(id);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/users")
    @Operation(summary = "Create user")
    public ResponseEntity<?> createUser(@Valid @RequestBody AdminUserCreateRequest request) {
        if (profileRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
        }

        ProfileDto dto = new ProfileDto();
        dto.setUsername(request.getUsername());
        dto.setFullName(request.getFullName());
        dto.setPhone(request.getPhone());
        dto.setPosition(request.getPosition());
        dto.setRole(jwtTokenUtil.normalizeRole(request.getRole()));
        dto.setIsActive(request.getIsActive());

        return ResponseEntity.ok(profileService.createProfile(dto, request.getPassword()));
    }

    @PutMapping("/users/{id}")
    @Operation(summary = "Update user")
    public ResponseEntity<?> updateUser(@PathVariable UUID id, @Valid @RequestBody AdminUserUpdateRequest request) {
        ProfileDto dto = new ProfileDto();
        dto.setFullName(request.getFullName());
        dto.setPhone(request.getPhone());
        dto.setPosition(request.getPosition());
        dto.setRole(jwtTokenUtil.normalizeRole(request.getRole()));
        dto.setIsActive(request.getIsActive());
        try {
            return ResponseEntity.ok(profileService.updateProfile(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/users/{id}/status")
    @Operation(summary = "Set active status for user")
    public ResponseEntity<?> setUserStatus(@PathVariable UUID id, @RequestParam boolean active) {
        ProfileDto profile = profileService.getProfileById(id);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        profile.setIsActive(active);
        profileService.updateProfile(id, profile);
        return ResponseEntity.ok(Map.of("status", "updated", "active", active));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete user")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
        if (profileService.getProfileById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        profileService.deleteProfile(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/metrics")
    @Operation(summary = "Get admin dashboard metrics")
    public ResponseEntity<?> getMetrics() {
        return ResponseEntity.ok(Map.of(
                "profiles", profileRepository.count(),
                "organizations", organizationRepository.count(),
                "inspections", inspectionRepository.count(),
                "violations", violationRepository.count()
        ));
    }
}
