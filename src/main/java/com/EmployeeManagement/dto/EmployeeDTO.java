package com.EmployeeManagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
    private Long id;
    private String email;
    private String name;
    private String mobile;
    private String address;
    private String role;
    private String imageUrl;
    private String skills;
    private String jobRole;
    private String companyInfo;
}