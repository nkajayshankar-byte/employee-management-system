package com.EmployeeManagement.service;

import com.EmployeeManagement.dto.EmployeeDTO;
import com.EmployeeManagement.entity.Job;
import com.EmployeeManagement.entity.JobApplication;
import com.EmployeeManagement.entity.Role;
import com.EmployeeManagement.entity.User;
import com.EmployeeManagement.mapper.EmployeeMapper;
import com.EmployeeManagement.dao.ApplicationDAO;
import com.EmployeeManagement.dao.JobDAO;
import com.EmployeeManagement.dao.UserDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class EmployeeService {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private ApplicationDAO applicationDAO;

    @Autowired
    private JobDAO jobDAO;

    public EmployeeDTO addEmployee(EmployeeDTO dto) {

        if (userDAO.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = employeeMapper.toEntity(dto);

        if (user.getRole() == null) {
            user.setRole(Role.EMPLOYEE);
        }

        user.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        user.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));

        userDAO.save(user);

        return employeeMapper.toDTO(user);
    }

    public List<EmployeeDTO> getUsersByRole(String role) {

        List<User> users;

        if (role == null || role.trim().isEmpty()) {
            users = userDAO.findAll();
        } else {
            try {
                Role enumRole = Role.valueOf(role.toUpperCase());
                users = userDAO.findByRole(enumRole);
            } catch (Exception e) {
                users = new ArrayList<>();
            }
        }

        List<EmployeeDTO> dtoList = new ArrayList<>();

        for (User user : users) {
            EmployeeDTO edto = employeeMapper.toDTO(user);
            
            // If jobRole is missing, try to resolve it from applications
            if ((user.getJobRole() == null || user.getJobRole().isEmpty()) && user.getRole() == Role.EMPLOYEE) {
                List<JobApplication> apps = applicationDAO.findByEmployeeId(user.getId());
                for (JobApplication app : apps) {
                    if ("HIRED".equals(app.getStatus())) {
                        Optional<Job> jobOpt = jobDAO.findById(app.getJobId());
                        if (jobOpt.isPresent()) {
                            String title = jobOpt.get().getTitle();
                            edto.setJobRole(title);
                            // Save it back to the user entity for future
                            user.setJobRole(title);
                            userDAO.save(user);
                            break;
                        }
                    }
                }
            }
            dtoList.add(edto);
        }

        return dtoList;
    }

    public EmployeeDTO getEmployeeById(String id) {
        Optional<User> optional = userDAO.findById(id);
        return optional.map(employeeMapper::toDTO).orElse(null);
    }

    public EmployeeDTO getEmployeeByEmail(String email) {
        Optional<User> optional = userDAO.findByEmail(email);
        return optional.map(employeeMapper::toDTO).orElse(null);
    }

    public EmployeeDTO updateEmployee(String id, EmployeeDTO dto) {

        Optional<User> optional = userDAO.findById(id);

        if (optional.isEmpty()) return null;

        User user = optional.get();

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setMobile(dto.getMobile());
        user.setAddress(dto.getAddress());
        user.setSkills(dto.getSkills());
        user.setJobRole(dto.getJobRole());
        user.setCompanyInfo(dto.getCompanyInfo());
        if (dto.getRole() != null) {
            try {
                user.setRole(Role.valueOf(dto.getRole().toUpperCase()));
            } catch (Exception e) {
                // ignore invalid role
            }
        }
        user.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));

        userDAO.save(user);

        return employeeMapper.toDTO(user);
    }

    public boolean deleteEmployee(String id) {
        if (userDAO.findById(id).isEmpty()) return false;
        userDAO.deleteById(id);
        return true;
    }

    public void bulkDeleteEmployees(List<String> ids) {
        for (String id : ids) {
            userDAO.deleteById(id);
        }
    }

    public List<EmployeeDTO> searchEmployees(String term) {

        List<User> employees = userDAO.findByRole(Role.EMPLOYEE);
        List<EmployeeDTO> result = new ArrayList<>();

        String search = term.toLowerCase();

        for (User emp : employees) {
            boolean match =
                    (emp.getName() != null && emp.getName().toLowerCase().contains(search)) ||
                    (emp.getEmail() != null && emp.getEmail().toLowerCase().contains(search)) ||
                    (emp.getMobile() != null && emp.getMobile().contains(term));

            if (match) {
                EmployeeDTO edto = employeeMapper.toDTO(emp);
                // Dynamic resolution
                if ((emp.getJobRole() == null || emp.getJobRole().isEmpty())) {
                    List<JobApplication> apps = applicationDAO.findByEmployeeId(emp.getId());
                    for (JobApplication app : apps) {
                        if ("HIRED".equals(app.getStatus())) {
                            Optional<Job> jobOpt = jobDAO.findById(app.getJobId());
                            if (jobOpt.isPresent()) {
                                String title = jobOpt.get().getTitle();
                                edto.setJobRole(title);
                                emp.setJobRole(title);
                                userDAO.save(emp);
                                break;
                            }
                        }
                    }
                }
                result.add(edto);
            }
        }

        return result;
    }

    @Autowired
    private FileStorageService fileStorageService;

    public String uploadImage(String userId, MultipartFile file) throws IOException {

        Optional<User> optional = userDAO.findById(userId);

        if (optional.isEmpty()) return null;

        User user = optional.get();

        String fileUrl = fileStorageService.save(file);

        user.setImageUrl(fileUrl);

        userDAO.save(user);

        return fileUrl;
    }
}