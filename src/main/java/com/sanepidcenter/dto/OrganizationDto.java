package com.sanepidcenter.dto;

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
    private String name;
    private String shortName;
    private String registrationNumber;
    private OrganizationTypeDto type;
    private String address;
    private String city;
    private String directorName;
    private String phone;
    private String email;
    private Integer employeeCount;
    private String riskCategory;
    private String notes;
}
