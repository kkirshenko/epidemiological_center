package com.sanepidcenter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for OrganizationType entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationTypeDto {
    private Integer id;
    private String name;
    private String description;
}
