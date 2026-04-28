package com.sanepidcenter.repository;

import com.sanepidcenter.model.Violation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Violation entity operations.
 */
@Repository
public interface ViolationRepository extends JpaRepository<Violation, UUID> {

    @Query("SELECT v FROM Violation v " +
           "JOIN FETCH v.inspection i " +
           "JOIN FETCH i.organization " +
           "JOIN FETCH i.type " +
           "JOIN FETCH i.inspector")
    List<Violation> findAllWithDetails();

    @Query("SELECT v FROM Violation v " +
           "JOIN FETCH v.inspection i " +
           "JOIN FETCH i.organization " +
           "JOIN FETCH i.type " +
           "JOIN FETCH i.inspector " +
           "WHERE v.id = :id")
    Optional<Violation> findByIdWithDetails(@Param("id") UUID id);
    
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
