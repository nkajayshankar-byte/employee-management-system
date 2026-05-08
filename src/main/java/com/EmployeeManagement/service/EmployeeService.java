package com.EmployeeManagement.service;

import com.EmployeeManagement.dao.*;
import com.EmployeeManagement.dto.EmployeeDTO;
import com.EmployeeManagement.entity.Role;
import com.EmployeeManagement.entity.User;
import com.EmployeeManagement.mapper.EmployeeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FileStorageService fileStorageService;

    public List<EmployeeDTO> getUsersByRole(String roleName) {
        if (roleName != null) {
            try {
                Role role = Role.valueOf(roleName.toUpperCase());
                return userDAO.findByRole(role).stream()
                        .map(employeeMapper::toDTO)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                return List.of();
            }
        }
        return userDAO.findAll().stream()
                .map(employeeMapper::toDTO)
                .collect(Collectors.toList());
    }

    public EmployeeDTO getEmployeeById(Long id) {
        return userDAO.findById(id)
                .map(employeeMapper::toDTO)
                .orElse(null);
    }

    public EmployeeDTO getEmployeeByEmail(String email) {
        return userDAO.findByEmail(email)
                .map(employeeMapper::toDTO)
                .orElse(null);
    }

    public List<EmployeeDTO> searchEmployees(String term) {
        String lowerTerm = term.toLowerCase();
        return userDAO.findAll().stream()
                .filter(u -> (u.getName() != null && u.getName().toLowerCase().contains(lowerTerm)) ||
                             u.getEmail().toLowerCase().contains(lowerTerm))
                .map(employeeMapper::toDTO)
                .collect(Collectors.toList());
    }

    public EmployeeDTO addEmployee(EmployeeDTO dto) {
        User user = employeeMapper.toEntity(dto);
        if (user.getPassword() == null) {
            user.setPassword(passwordEncoder.encode("Welcome123")); // Default password
        }
        user.onCreate();
        User saved = userDAO.save(user);
        return employeeMapper.toDTO(saved);
    }

    public EmployeeDTO updateEmployee(Long id, EmployeeDTO dto) {
        Optional<User> existing = userDAO.findById(id);
        if (existing.isPresent()) {
            User user = existing.get();
            user.setName(dto.getName());
            user.setEmail(dto.getEmail());
            user.setMobile(dto.getMobile());
            user.setAddress(dto.getAddress());
            user.setSkills(dto.getSkills());
            user.setJobRole(dto.getJobRole());
            user.setCompanyInfo(dto.getCompanyInfo());
            user.setImageUrl(dto.getImageUrl());
            if (dto.getRole() != null) {
                user.setRole(Role.valueOf(dto.getRole().toUpperCase()));
            }
            user.onUpdate();
            User saved = userDAO.save(user);
            return employeeMapper.toDTO(saved);
        }
        return null;
    }

    public boolean deleteEmployee(Long id) {
        if (userDAO.findById(id).isPresent()) {
            // Relational integrity handled by MySQL ON DELETE CASCADE
            userDAO.deleteById(id);
            return true;
        }
        return false;
    }

    public void bulkDeleteEmployees(List<Long> ids) {
        for (Long id : ids) {
            deleteEmployee(id);
        }
    }

    public String uploadImage(Long id, MultipartFile file) throws IOException {
        Optional<User> userOpt = userDAO.findById(id);
        if (userOpt.isPresent()) {
            String url = fileStorageService.save(file);
            User user = userOpt.get();
            user.setImageUrl(url);
            userDAO.save(user);
            return url;
        }
        return null;
    }
}