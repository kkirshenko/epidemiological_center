package com.sanepidcenter.repository;

import com.sanepidcenter.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Organization entity operations.
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    
    List<Organization> findByNameContainingIgnoreCase(String name);
    
    List<Organization> findByCityContainingIgnoreCase(String city);
    
    List<Organization> findByRiskCategory(String riskCategory);
    
    List<Organization> findByIsActiveTrue();
    
    Optional<Organization> findByRegistrationNumber(String registrationNumber);
    
    @Query("SELECT o FROM Organization o WHERE LOWER(o.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(o.shortName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(o.type.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(o.city) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(o.riskCategory) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR (LOWER(:query) = 'низкий' AND o.riskCategory = 'low') " +
           "OR (LOWER(:query) = 'средний' AND o.riskCategory = 'medium') " +
           "OR (LOWER(:query) = 'высокий' AND o.riskCategory = 'high') " +
           "OR (LOWER(:query) = 'критический' AND o.riskCategory = 'critical')")
    List<Organization> findAllFieldsContainingIgnoreCase(@Param("query") String query);
    
    @Query("SELECT o FROM Organization o JOIN FETCH o.type WHERE o.id = :id")
    Optional<Organization> findByIdWithDetails(@Param("id") UUID id);
}
