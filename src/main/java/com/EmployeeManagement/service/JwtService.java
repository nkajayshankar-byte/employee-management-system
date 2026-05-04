package com.EmployeeManagement.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email, String role, String userId) {
        return Jwts.builder() 
                .setSubject(email)
                .claim("role", role)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    public String extractEmail(String token) {
        Claims claims = extractAllClaims(token);
        return claims != null ? claims.getSubject() : null;
    }

    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        return claims != null ? claims.get("role", String.class) : null;
    }
    public String extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims != null ? claims.get("userId", String.class) : null;
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);

            if (claims == null) {
                return false;
            }

            return claims.getExpiration().after(new Date());

        } catch (Exception e) {
            return false;
        }
    }
}