package com.sanepidcenter.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for Organization entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDto {
    private UUID id;
    @NotBlank
    private String name;
    private String shortName;
    private String registrationNumber;
    @NotNull
    private OrganizationTypeDto type;
    @NotBlank
    private String address;
    @NotBlank
    private String city;
    @NotBlank
    private String directorName;
    @Pattern(regexp = "^[+0-9()\\-\\s]{7,20}$", message = "Invalid phone format")
    private String phone;
    @Email
    private String email;
    @Min(0)
    private Integer employeeCount;
    @NotBlank
    private String riskCategory;
    private String notes;
}
