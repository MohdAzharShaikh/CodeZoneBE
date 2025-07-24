// src/main/java/com/codezone/backend/controllers/AuthController.java
package com.codezone.backend.controllers;

import com.codezone.backend.entities.User;
import com.codezone.backend.dto.RegisterRequest;
import com.codezone.backend.dto.LoginRequest; // NEW
import com.codezone.backend.dto.JwtResponse; // NEW
import com.codezone.backend.services.UserService;
import com.codezone.backend.security.JwtUtils; // NEW
import com.codezone.backend.security.UserDetailsServiceImpl; // NEW

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager; // NEW
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // NEW
import org.springframework.security.core.Authentication; // NEW
import org.springframework.security.core.context.SecurityContextHolder; // NEW
import org.springframework.security.core.userdetails.UserDetails; // NEW
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager; // NEW
    private final JwtUtils jwtUtils; // NEW
    private final UserDetailsServiceImpl userDetailsService; // NEW

    @Autowired
    public AuthController(UserService userService, AuthenticationManager authenticationManager, JwtUtils jwtUtils, UserDetailsServiceImpl userDetailsService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            if (registerRequest.getUsername() == null || registerRequest.getUsername().trim().isEmpty() ||
                registerRequest.getPassword() == null || registerRequest.getPassword().trim().isEmpty() ||
                registerRequest.getEmail() == null || registerRequest.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Username, password, and email cannot be empty.");
            }

            userService.registerNewUser(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully!");
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred during registration.");
        }
    }

    @PostMapping("/login") // NEW: Login endpoint
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            // Get User details from the loaded User (from database)
            // Assuming UserDetailsServiceImpl loads User entity via UserRepository
            User userEntity = userService.findByUsername(loginRequest.getUsername())
                                .orElseThrow(() -> new RuntimeException("User not found after authentication."));


            return ResponseEntity.ok(new JwtResponse(jwt, userEntity.getId(), userEntity.getUsername(), userEntity.getEmail()));
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login failed: Invalid username or password.");
        }
    }
}