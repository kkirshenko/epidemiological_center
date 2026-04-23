package com.sanepidcenter.repository;

import com.sanepidcenter.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Profile entity operations.
 */
@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID> {
    
    List<Profile> findByRole(String role);
    
    List<Profile> findByIsActiveTrue();
    
    Optional<Profile> findByFullNameContainingIgnoreCase(String fullName);
    
    Optional<Profile> findByUsername(String username);
    
    boolean existsByUsername(String username);
}
