package com.sanepidcenter.repository;

import com.sanepidcenter.model.Inspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Inspection entity operations.
 */
@Repository
public interface InspectionRepository extends JpaRepository<Inspection, UUID> {

    @Query("SELECT DISTINCT i FROM Inspection i " +
           "JOIN FETCH i.organization " +
           "JOIN FETCH i.type " +
           "JOIN FETCH i.inspector")
    List<Inspection> findAllWithDetails();
    
    List<Inspection> findByOrganizationId(UUID organizationId);
    
    List<Inspection> findByInspectorId(UUID inspectorId);
    
    List<Inspection> findByStatus(String status);
    
    List<Inspection> findByResult(String result);
    
    List<Inspection> findByScheduledDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT i FROM Inspection i WHERE LOWER(i.organization.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(i.type.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR CAST(i.scheduledDate AS string) LIKE CONCAT('%', :query, '%') " +
           "OR LOWER(i.status) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR (LOWER(:query) = 'запланирована' AND i.status = 'planned') " +
           "OR (LOWER(:query) = 'в процессе' AND i.status = 'in_progress') " +
           "OR (LOWER(:query) = 'завершена' AND i.status = 'completed') " +
           "OR (LOWER(:query) = 'отменена' AND i.status = 'cancelled') " +
           "OR LOWER(i.result) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR (LOWER(:query) = 'ожидается' AND i.result = 'pending') " +
           "OR (LOWER(:query) = 'удовлетворительно' AND i.result = 'satisfactory') " +
           "OR (LOWER(:query) = 'неудовлетворительно' AND i.result = 'unsatisfactory') " +
           "OR (LOWER(:query) = 'критическое' AND i.result = 'critical')")
    List<Inspection> findAllFieldsContainingIgnoreCase(@Param("query") String query);
    
    @Query("SELECT DISTINCT i FROM Inspection i " +
           "JOIN FETCH i.organization " +
           "JOIN FETCH i.type " +
           "JOIN FETCH i.inspector " +
           "WHERE i.id = :id")
    Optional<Inspection> findByIdWithDetails(@Param("id") UUID id);
    
    List<Inspection> findByStatusOrderByScheduledDateDesc(String status);
}
