package com.sanepidcenter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for Profile entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDto {
    private UUID id;
    private String username;
    private String fullName;
    private String phone;
    private String position;
    private String role;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
