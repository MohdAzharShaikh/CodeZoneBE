// src/main/java/com/codezone/backend/repositories/UserRepository.java
package com.codezone.backend.repositories;

import com.codezone.backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // Marks this interface as a Spring Data JPA repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Custom method to find a user by username
    Optional<User> findByUsername(String username);

    // Custom method to find a user by email
    Optional<User> findByEmail(String email);

    // You can add more custom query methods here as needed
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}