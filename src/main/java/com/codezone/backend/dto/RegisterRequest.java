// src/main/java/com/codezone/backend/dto/RegisterRequest.java
package com.codezone.backend.dto;

// This DTO defines the data structure for user registration requests from the frontend
public class RegisterRequest {
    private String username;
    private String password;
    private String email;

    // Constructors
    public RegisterRequest() {
    }

    public RegisterRequest(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}