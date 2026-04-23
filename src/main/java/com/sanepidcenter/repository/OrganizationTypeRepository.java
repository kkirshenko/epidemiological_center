package com.sanepidcenter.repository;

import com.sanepidcenter.model.OrganizationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for OrganizationType entity operations.
 */
@Repository
public interface OrganizationTypeRepository extends JpaRepository<OrganizationType, Integer> {
    
    Optional<OrganizationType> findByName(String name);
    
    List<OrganizationType> findAllByOrderByNameAsc();
}
