package com.sanepidcenter.service;

import com.sanepidcenter.dto.ProfileDto;
import com.sanepidcenter.model.Profile;
import com.sanepidcenter.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing user profiles.
 */
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    public List<ProfileDto> getAllProfiles() {
        return profileRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ProfileDto getProfileById(UUID id) {
        return profileRepository.findById(id)
                .map(this::toDto)
                .orElse(null);
    }

    public ProfileDto getProfileByUsername(String username) {
        return profileRepository.findByUsername(username)
                .map(this::toDto)
                .orElse(null);
    }

    @Transactional
    public ProfileDto createProfile(ProfileDto dto, String rawPassword) {
        Profile profile = new Profile();
        profile.setId(UUID.randomUUID());
        profile.setUsername(dto.getUsername());
        profile.setPassword(passwordEncoder.encode(rawPassword));
        profile.setFullName(dto.getFullName());
        profile.setPhone(dto.getPhone());
        profile.setPosition(dto.getPosition());
        profile.setRole(dto.getRole());
        profile.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        
        profileRepository.save(profile);
        return toDto(profile);
    }

    @Transactional
    public ProfileDto updateProfile(UUID id, ProfileDto dto) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found with id: " + id));
        
        profile.setFullName(dto.getFullName());
        profile.setPhone(dto.getPhone());
        profile.setPosition(dto.getPosition());
        profile.setRole(dto.getRole());
        profile.setIsActive(dto.getIsActive());
        
        profileRepository.save(profile);
        return toDto(profile);
    }

    @Transactional
    public void deleteProfile(UUID id) {
        profileRepository.deleteById(id);
    }

    @Transactional
    public void toggleProfileStatus(UUID id) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found with id: " + id));
        profile.setIsActive(!profile.getIsActive());
        profileRepository.save(profile);
    }

    private ProfileDto toDto(Profile profile) {
        ProfileDto dto = new ProfileDto();
        dto.setId(profile.getId());
        dto.setUsername(profile.getUsername());
        dto.setFullName(profile.getFullName());
        dto.setPhone(profile.getPhone());
        dto.setPosition(profile.getPosition());
        dto.setRole(profile.getRole());
        dto.setIsActive(profile.getIsActive());
        dto.setCreatedAt(profile.getCreatedAt());
        dto.setUpdatedAt(profile.getUpdatedAt());
        return dto;
    }
}
