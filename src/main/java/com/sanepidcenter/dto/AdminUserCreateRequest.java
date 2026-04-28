package com.sanepidcenter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AdminUserCreateRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String fullName;

    @NotBlank
    @Pattern(regexp = "^[+0-9()\\-\\s]{7,20}$", message = "Invalid phone format")
    private String phone;

    @NotBlank
    private String position;

    @NotBlank
    private String role;

    private Boolean isActive = true;
}
