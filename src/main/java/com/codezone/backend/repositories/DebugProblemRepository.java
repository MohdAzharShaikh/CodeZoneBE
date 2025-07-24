// src/main/java/com/codezone/backend/repositories/DebugProblemRepository.java
package com.codezone.backend.repositories;

import com.codezone.backend.entities.DebugProblem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DebugProblemRepository extends JpaRepository<DebugProblem, Long> {
    // Spring Data JPA will automatically provide methods like save, findById, findAll, etc.
}