package com.EmployeeManagement.mapper;

import com.EmployeeManagement.dto.EmployeeDTO;
import com.EmployeeManagement.entity.User;
import com.EmployeeManagement.entity.Role;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    public EmployeeDTO toDTO(User user) {
        if (user == null) return null;

        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setMobile(user.getMobile());
        dto.setAddress(user.getAddress());
        dto.setSkills(user.getSkills());
        dto.setJobRole(user.getJobRole());
        dto.setCompanyInfo(user.getCompanyInfo());
        dto.setImageUrl(user.getImageUrl());

        if (user.getRole() != null) {
            dto.setRole(user.getRole().name());
        }

        return dto;
    }

    public User toEntity(EmployeeDTO dto) {
        if (dto == null) return null;

        User user = new User();
        user.setId(dto.getId());
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setMobile(dto.getMobile());
        user.setAddress(dto.getAddress());
        user.setSkills(dto.getSkills());
        user.setJobRole(dto.getJobRole());
        user.setCompanyInfo(dto.getCompanyInfo());
        user.setImageUrl(dto.getImageUrl());

        if (dto.getRole() != null) {
            try {
                user.setRole(Role.valueOf(dto.getRole().toUpperCase()));
            } catch (Exception e) {
                // ignore invalid role
            }
        }

        return user;
    }
}
