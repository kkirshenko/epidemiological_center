package com.sanepidcenter.service;

import com.sanepidcenter.model.Organization;
import com.sanepidcenter.model.OrganizationType;
import com.sanepidcenter.repository.OrganizationRepository;
import com.sanepidcenter.repository.OrganizationTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationTypeRepository organizationTypeRepository;

    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }

    public List<Organization> getActiveOrganizations() {
        return organizationRepository.findByIsActiveTrue();
    }

    public Optional<Organization> getOrganizationById(UUID id) {
        return organizationRepository.findByIdWithDetails(id);
    }

    public Optional<Organization> getOrganizationByRegistrationNumber(String registrationNumber) {
        return organizationRepository.findByRegistrationNumber(registrationNumber);
    }

    public List<Organization> searchByName(String name) {
        return organizationRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Organization> searchAllFields(String query) {
        return organizationRepository.findAllFieldsContainingIgnoreCase(query);
    }

    public List<Organization> sortOrganizations(List<Organization> organizations, String sortBy, String sortDir) {
        if (sortBy == null || sortBy.isEmpty()) {
            return organizations;
        }
        
        boolean ascending = sortDir == null || "asc".equalsIgnoreCase(sortDir);
        
        return organizations.stream()
            .sorted((o1, o2) -> {
                int cmp = 0;
                switch (sortBy.toLowerCase()) {
                    case "name":
                        cmp = o1.getName().compareToIgnoreCase(o2.getName());
                        break;
                    case "shortname":
                        cmp = o1.getShortName().compareToIgnoreCase(o2.getShortName());
                        break;
                    case "city":
                        cmp = o1.getCity().compareToIgnoreCase(o2.getCity());
                        break;
                    case "riskcategory":
                        cmp = o1.getRiskCategory().compareToIgnoreCase(o2.getRiskCategory());
                        break;
                    default:
                        cmp = 0;
                }
                return ascending ? cmp : -cmp;
            })
            .toList();
    }

    public List<Organization> searchByCity(String city) {
        return organizationRepository.findByCityContainingIgnoreCase(city);
    }

    public List<Organization> getByRiskCategory(String riskCategory) {
        return organizationRepository.findByRiskCategory(riskCategory);
    }

    @Transactional
    public Organization createOrganization(Organization organization) {
        if (organization.getId() == null) {
            organization.setId(UUID.randomUUID());
        }

        Integer typeId = organization.getType() != null ? organization.getType().getId() : null;
        if (typeId == null) {
            throw new IllegalArgumentException("Organization type is required");
        }

        OrganizationType managedType = organizationTypeRepository.findById(typeId)
                .orElseThrow(() -> new IllegalArgumentException("Organization type not found: " + typeId));
        organization.setType(managedType);

        return organizationRepository.save(organization);
    }

    @Transactional
    public Organization updateOrganization(UUID id, Organization updatedOrganization) {
        return organizationRepository.findById(id)
                .map(existing -> {
                    existing.setName(updatedOrganization.getName());
                    existing.setShortName(updatedOrganization.getShortName());
                    existing.setAddress(updatedOrganization.getAddress());
                    existing.setCity(updatedOrganization.getCity());
                    existing.setDirectorName(updatedOrganization.getDirectorName());
                    existing.setPhone(updatedOrganization.getPhone());
                    existing.setEmail(updatedOrganization.getEmail());
                    existing.setEmployeeCount(updatedOrganization.getEmployeeCount());
                    existing.setRiskCategory(updatedOrganization.getRiskCategory());
                    existing.setNotes(updatedOrganization.getNotes());

                    Integer typeId = updatedOrganization.getType() != null ? updatedOrganization.getType().getId() : null;
                    if (typeId == null) {
                        throw new IllegalArgumentException("Organization type is required");
                    }
                    OrganizationType managedType = organizationTypeRepository.findById(typeId)
                            .orElseThrow(() -> new IllegalArgumentException("Organization type not found: " + typeId));
                    existing.setType(managedType);

                    return organizationRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Organization not found with id: " + id));
    }

    @Transactional
    public void deleteOrganization(UUID id) {
        organizationRepository.deleteById(id);
    }
}
