// src/main/java/com/codezone/backend/security/JwtUtils.java
package com.codezone.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
import org.springframework.security.core.Authentication; // For getting authenticated user details
import org.springframework.security.core.userdetails.UserDetails; // For user details


@Component // Marks this as a Spring component
public class JwtUtils {

    @Value("${jwt.secret}") // Injects the JWT secret from application.properties
    private String jwtSecret;

    @Value("${jwt.expirationMs}") // Injects the JWT expiration time from application.properties
    private int jwtExpirationMs;

    // Generate JWT token
    public String generateJwtToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject((userPrincipal.getUsername())) // User's username as subject
                .setIssuedAt(new Date()) // Token issuance date
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs)) // Token expiration date
                .signWith(key(), SignatureAlgorithm.HS256) // Sign with secret key and algorithm
                .compact(); // Build and compact the token
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // Get username from JWT token
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    // Validate JWT token
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
        } catch (MalformedJwtException e) {
            System.err.println("Invalid JWT token: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            System.err.println("JWT token is expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.err.println("JWT token is unsupported: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("JWT claims string is empty: " + e.getMessage());
        }
        return false;
    }
}