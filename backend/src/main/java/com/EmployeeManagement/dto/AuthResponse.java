package com.EmployeeManagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String userId; // Kept as String for frontend compat, populated from Long
    private String email;
    private String name;
    private String role;
    private String message;
    
    // Additional fields for profile
    private String imageUrl;
    private String skills;
    private String address;
    private String jobTitle;
    private String companyInfo;

    public AuthResponse(String token, String userId, String email, String name, String role, String message) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.role = role;
        this.message = message;
    }
}