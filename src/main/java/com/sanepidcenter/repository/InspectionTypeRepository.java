package com.sanepidcenter.repository;

import com.sanepidcenter.model.InspectionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for InspectionType entity operations.
 */
@Repository
public interface InspectionTypeRepository extends JpaRepository<InspectionType, Integer> {
    
    Optional<InspectionType> findByCode(String code);
    
    Optional<InspectionType> findByName(String name);
    
    List<InspectionType> findAllByOrderByNameAsc();
}
