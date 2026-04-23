package com.sanepidcenter.repository;

import com.sanepidcenter.model.Violation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Violation entity operations.
 */
@Repository
public interface ViolationRepository extends JpaRepository<Violation, UUID> {
    
    List<Violation> findByInspectionId(UUID inspectionId);
    
    List<Violation> findByResolved(Boolean resolved);
    
    List<Violation> findBySeverity(String severity);
    
    @Query("SELECT v FROM Violation v WHERE LOWER(v.severity) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(v.inspection.organization.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR CAST(v.inspection.scheduledDate AS string) LIKE CONCAT('%', :query, '%') " +
           "OR (LOWER(:query) = 'устранено' AND v.resolved = true) " +
           "OR ((LOWER(:query) = 'не устранено' OR LOWER(:query) = 'неустранено') AND v.resolved = false)")
    List<Violation> findAllFieldsContainingIgnoreCase(@Param("query") String query);
}
