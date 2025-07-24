package com.codezone.backend.controllers;

import com.codezone.backend.entities.DebugProblem;
import com.codezone.backend.repositories.DebugProblemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * This controller handles HTTP requests related to debugging problems.
 * It provides an endpoint for clients to fetch a list of all available debug problems.
 */
@RestController
@RequestMapping("/api")
public class DebugProblemController {

    @Autowired
    private DebugProblemRepository debugProblemRepository;

    /**
     * Handles GET requests to /api/debug-problems.
     * This endpoint is protected by Spring Security and requires user authentication.
     *
     * @return A ResponseEntity containing a list of all DebugProblem entities.
     */
    @GetMapping("/debug-problems")
    public ResponseEntity<List<DebugProblem>> getAllDebugProblems() {
        // Retrieve all problems from the repository
        List<DebugProblem> problems = debugProblemRepository.findAll();
        // Return the list with an HTTP 200 OK status
        return ResponseEntity.ok(problems);
    }
}