package com.EmployeeManagement.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.EmployeeManagement.dto.AuthRequest;
import com.EmployeeManagement.dto.AuthResponse;
import com.EmployeeManagement.entity.Job;
import com.EmployeeManagement.entity.JobApplication;
import com.EmployeeManagement.entity.Role;
import com.EmployeeManagement.entity.User;
import com.EmployeeManagement.dao.ApplicationDAO;
import com.EmployeeManagement.dao.JobDAO;
import com.EmployeeManagement.dao.UserDAO;

@Service
public class AuthService {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EmailService emailService;
    
    private final ConcurrentHashMap<String, String> otpStore = new ConcurrentHashMap<>();


    public AuthResponse signup(AuthRequest request) {

        Optional<User> existingUser = userDAO.findByEmail(request.getEmail());

        if (existingUser.isPresent()) {
            return new AuthResponse(null, "User already exists");
        }

        User user = new User();
        if (request.getEmail() == null || request.getPassword() == null) {
            return new AuthResponse(null, "Email and password are required");
        }

        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        if (request.getRole() == null || request.getRole().trim().isEmpty()) {
            user.setRole(Role.USER);
        } else {
            try {
                Role requestedRole = Role.valueOf(request.getRole().toUpperCase());
                if (requestedRole == Role.EMPLOYEE) {
                    return new AuthResponse(null, "Employee accounts must be created by an administrator");
                }
                user.setRole(requestedRole);
            } catch (IllegalArgumentException e) {
                user.setRole(Role.USER);
            }
        }

        if (user.getName() == null || user.getName().trim().isEmpty()) {
            user.setName(extractNameFromEmail(user.getEmail()));
        }

        userDAO.save(user);

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name(), user.getId());

        return new AuthResponse(token, "Signup successful");
    }

    @Autowired
    private ApplicationDAO applicationDAO;
    
    @Autowired
    private JobDAO jobDAO;

    public Map<String, Object> loginUser(AuthRequest request) {

        Optional<User> userOpt = userDAO.findByEmail(request.getEmail());

        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with email: " + request.getEmail());
        }

        if (!passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        User user = userOpt.get();
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name(), user.getId());

        // Look up hired job title for EMPLOYEE users
        String jobTitle = "";
        if (user.getRole() == Role.EMPLOYEE) {
            List<JobApplication> hiredApps = applicationDAO.findByEmployeeId(user.getId());
            for (JobApplication app : hiredApps) {
                if ("HIRED".equals(app.getStatus())) {
                    Optional<Job> jobOpt = jobDAO.findById(app.getJobId());
                    if (jobOpt.isPresent()) {
                        jobTitle = jobOpt.get().getTitle();
                        break;
                    }
                }
            }
        }

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("token", token);
        responseMap.put("userId", user.getId());
        responseMap.put("email", user.getEmail());
        responseMap.put("name", user.getName() != null ? user.getName() : "");
        responseMap.put("role", user.getRole().name());
        responseMap.put("jobTitle", jobTitle);
        responseMap.put("imageUrl", user.getImageUrl() != null ? user.getImageUrl() : "");
        responseMap.put("skills", user.getSkills() != null ? user.getSkills() : "");
        responseMap.put("jobRole", user.getJobRole() != null ? user.getJobRole() : "");
        responseMap.put("address", user.getAddress() != null ? user.getAddress() : "");

        return responseMap;
    }

    public AuthResponse generateAndSendOtp(String email) {

        Optional<User> userOpt = userDAO.findByEmail(email);

        if (userOpt.isEmpty()) {
            return new AuthResponse(null, "User not found");
        }
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStore.put(email, otp);
        emailService.sendOtpEmail(email, otp);

        return new AuthResponse(null, "OTP sent to your registered email");
    }


    public boolean verifyOtp(String email, String otp) {
        String stored = otpStore.get(email);
        return stored != null && stored.equals(otp);
    }


    public AuthResponse resetPassword(String email, String newPassword) {

        Optional<User> userOpt = userDAO.findByEmail(email);

        if (userOpt.isEmpty()) {
            return new AuthResponse(null, "User not found");
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));

        userDAO.save(user);
        otpStore.remove(email);

        // Send confirmation email asynchronously — does NOT block if email fails
        emailService.sendPasswordResetSuccessEmail(email);

        return new AuthResponse(null, "Password reset successful");
    }

    private String extractNameFromEmail(String email) {

        if (email == null || !email.contains("@")) {
            return "Unknown";
        }

        String namePart = email.split("@")[0];

        String[] parts = namePart.split("\\.");

        StringBuilder name = new StringBuilder();

        for (String part : parts) {
            if (part.isEmpty()) continue;

            name.append(Character.toUpperCase(part.charAt(0)))
                .append(part.substring(1).toLowerCase())
                .append(" ");
        }

        return name.toString().trim();
    }
}