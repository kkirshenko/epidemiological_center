package com.sanepidcenter.controller;

import com.sanepidcenter.dto.OrganizationDto;
import com.sanepidcenter.dto.OrganizationTypeDto;
import com.sanepidcenter.model.Organization;
import com.sanepidcenter.model.OrganizationType;
import com.sanepidcenter.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrganizationApiController {

    private final OrganizationService organizationService;

    @GetMapping
    public ResponseEntity<List<OrganizationDto>> getAllOrganizations() {
        List<OrganizationDto> organizations = organizationService.getAllOrganizations()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(organizations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationDto> getOrganizationById(@PathVariable UUID id) {
        return organizationService.getOrganizationById(id)
                .map(org -> ResponseEntity.ok(convertToDto(org)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createOrganization(@Valid @RequestBody OrganizationDto organizationDto) {
        try {
            Organization organization = convertToEntity(organizationDto);
            Organization created = organizationService.createOrganization(organization);
            return ResponseEntity.ok(convertToDto(created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrganization(
            @PathVariable UUID id,
            @Valid @RequestBody OrganizationDto organizationDto) {
        try {
            Organization organization = convertToEntity(organizationDto);
            Organization updated = organizationService.updateOrganization(id, organization);
            return ResponseEntity.ok(convertToDto(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrganization(@PathVariable UUID id) {
        organizationService.deleteOrganization(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<OrganizationDto>> searchOrganizations(@RequestParam String query) {
        List<OrganizationDto> organizations = organizationService.searchAllFields(query)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(organizations);
    }

    private OrganizationDto convertToDto(Organization organization) {
        OrganizationDto dto = new OrganizationDto();
        dto.setId(organization.getId());
        dto.setName(organization.getName());
        dto.setShortName(organization.getShortName());
        dto.setRegistrationNumber(organization.getRegistrationNumber());
        if (organization.getType() != null) {
            OrganizationTypeDto typeDto = new OrganizationTypeDto();
            typeDto.setId(organization.getType().getId());
            typeDto.setName(organization.getType().getName());
            typeDto.setDescription(organization.getType().getDescription());
            dto.setType(typeDto);
        }
        dto.setAddress(organization.getAddress());
        dto.setCity(organization.getCity());
        dto.setDirectorName(organization.getDirectorName());
        dto.setPhone(organization.getPhone());
        dto.setEmail(organization.getEmail());
        dto.setEmployeeCount(organization.getEmployeeCount());
        dto.setRiskCategory(organization.getRiskCategory());
        dto.setNotes(organization.getNotes());
        return dto;
    }

    private Organization convertToEntity(OrganizationDto dto) {
        Organization organization = new Organization();
        organization.setId(dto.getId());
        organization.setName(dto.getName());
        organization.setShortName(dto.getShortName());
        organization.setRegistrationNumber(dto.getRegistrationNumber());
        if (dto.getType() != null) {
            OrganizationType type = new OrganizationType();
            type.setId(dto.getType().getId());
            organization.setType(type);
        }
        organization.setAddress(dto.getAddress());
        organization.setCity(dto.getCity());
        organization.setDirectorName(dto.getDirectorName());
        organization.setPhone(dto.getPhone());
        organization.setEmail(dto.getEmail());
        organization.setEmployeeCount(dto.getEmployeeCount());
        organization.setRiskCategory(dto.getRiskCategory());
        organization.setNotes(dto.getNotes());
        return organization;
    }
}
