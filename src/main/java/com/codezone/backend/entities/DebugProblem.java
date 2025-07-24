// src/main/java/com/codezone/backend/entities/DebugProblem.java
package com.codezone.backend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity // Marks this class as a JPA entity, mapped to a database table
@Table(name = "debug_problems") // Specifies the table name in the database
public class DebugProblem {

    @Id // Marks this field as the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increments in MySQL
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT") // Use TEXT for longer descriptions
    private String description;

    @Column(nullable = false)
    private String language; // e.g., "java", "python", "javascript", "cpp"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String codeWithError; // The code snippet that has an error

    @Column(columnDefinition = "TEXT") // Optional: solution code
    private String solutionCode;

    // Constructors
    public DebugProblem() {
    }

    public DebugProblem(String title, String description, String language, String codeWithError, String solutionCode) {
        this.title = title;
        this.description = description;
        this.language = language;
        this.codeWithError = codeWithError;
        this.solutionCode = solutionCode;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCodeWithError() {
        return codeWithError;
    }

    public void setCodeWithError(String codeWithError) {
        this.codeWithError = codeWithError;
    }

    public String getSolutionCode() {
        return solutionCode;
    }

    public void setSolutionCode(String solutionCode) {
        this.solutionCode = solutionCode;
    }
}