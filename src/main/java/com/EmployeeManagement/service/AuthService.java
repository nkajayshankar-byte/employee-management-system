package com.EmployeeManagement.service;

import com.EmployeeManagement.dao.UserDAO;
import com.EmployeeManagement.dto.AuthRequest;
import com.EmployeeManagement.dto.AuthResponse;
import com.EmployeeManagement.entity.Role;
import com.EmployeeManagement.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

    private static class OtpDetails {
        String otp;
        long expiryTime;

        OtpDetails(String otp, long expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }
    }

    public static class TwoFactorSession {
        public int targetNumber;
        public String status;
        public long expiryTime;
        public int attemptsLeft;

        public TwoFactorSession(int targetNumber, long expiryTime) {
            this.targetNumber = targetNumber;
            this.expiryTime = expiryTime;
            this.status = "PENDING";
            this.attemptsLeft = 3;
        }
    }

    private final Map<String, OtpDetails> otpStorage = new ConcurrentHashMap<>();
    private final Map<String, TwoFactorSession> twoFactorStorage = new ConcurrentHashMap<>();

    public AuthResponse signup(AuthRequest signupRequest) {
        String email = signupRequest.getEmail() != null ? signupRequest.getEmail().toLowerCase().trim() : null;
        if (email == null || userDAO.existsByEmail(email)) {
            return new AuthResponse(null, null, null, null, null, "Email already exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        
        // Auto-generate name from email if not provided
        String name = signupRequest.getName();
        if (name == null || name.trim().isEmpty()) {
            if (email != null && email.contains("@")) {
                name = email.split("@")[0];
                if (name.length() > 0) {
                    name = name.substring(0, 1).toUpperCase() + name.substring(1);
                }
            }
        }
        user.setName(name);
        
        user.setMobile(signupRequest.getMobile());
        user.setAddress(signupRequest.getAddress());
        
        if (userDAO.findAll().isEmpty()) {
            user.setRole(Role.ADMIN);
        } else {
            String roleStr = signupRequest.getRole();
            if (roleStr != null && roleStr.equalsIgnoreCase("ADMIN")) {
                // Secure Admin Registration
                String secretKey = System.getenv("ADMIN_SIGNUP_KEY");
                if (secretKey == null || secretKey.isEmpty() || signupRequest.getAdminKey() == null || !signupRequest.getAdminKey().equals(secretKey)) {
                    return new AuthResponse(null, null, null, null, null, "INVALID_ADMIN_KEY");
                }
                user.setRole(Role.ADMIN);
            } else if (roleStr != null && !roleStr.isEmpty()) {
                try {
                    user.setRole(Role.valueOf(roleStr.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    user.setRole(Role.USER);
                }
            } else {
                user.setRole(Role.USER);
            }
        }
        
        user.onCreate();
        User savedUser = userDAO.save(user);

        String token = jwtService.generateToken(savedUser.getEmail(), savedUser.getRole().name(), savedUser.getId());
        
        try {
            emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getName() != null ? savedUser.getName() : "Valued Member");
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }

        AuthResponse response = new AuthResponse(token, savedUser.getId().toString(), savedUser.getEmail(), savedUser.getName(), savedUser.getRole().name(), "User registered successfully");
        response.setSkills(savedUser.getSkills());
        response.setAddress(savedUser.getAddress());
        return response;
    }

    public Map<String, Object> loginUser(AuthRequest loginRequest) {
        String email = loginRequest.getEmail() != null ? loginRequest.getEmail().toLowerCase().trim() : null;
        Optional<User> userOpt = userDAO.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                
                if (user.isTwoFactorEnabled()) {
                    Random random = new Random();
                    int targetNumber = 10 + random.nextInt(90);
                    
                    List<Integer> allNumbers = new ArrayList<>();
                    allNumbers.add(targetNumber);
                    while(allNumbers.size() < 4) {
                        int randNum = 10 + random.nextInt(90);
                        if(!allNumbers.contains(randNum)) {
                            allNumbers.add(randNum);
                        }
                    }
                    Collections.shuffle(allNumbers);
                    
                    long expiryTime = System.currentTimeMillis() + 2 * 60 * 1000;
                    twoFactorStorage.put(user.getEmail(), new TwoFactorSession(targetNumber, expiryTime));
                    
                    try {
                        emailService.send2faEmail(user.getEmail(), targetNumber, allNumbers);
                    } catch (Exception e) {
                        System.err.println("Failed to send 2FA email: " + e.getMessage());
                    }

                    Map<String, Object> result = new HashMap<>();
                    result.put("requires2fa", true);
                    result.put("targetNumber", targetNumber);
                    result.put("email", user.getEmail());
                    return result;
                }

                return complete2faLogin(user.getEmail());
            }
        }

        throw new RuntimeException("Invalid email or password");
    }

    public Map<String, Object> complete2faLogin(String email) {
        User user = userDAO.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name(), user.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId().toString());
        result.put("email", user.getEmail());
        result.put("name", user.getName());
        result.put("role", user.getRole().name());
        result.put("message", "Login successful");
        result.put("imageUrl", user.getImageUrl());
        result.put("jobTitle", user.getJobRole());
        result.put("skills", user.getSkills());
        result.put("address", user.getAddress());

        return result;
    }

    public String check2faStatus(String email) {
        TwoFactorSession session = twoFactorStorage.get(email);
        if (session == null) return "FAILED";
        if (System.currentTimeMillis() > session.expiryTime) {
            twoFactorStorage.remove(email);
            return "FAILED";
        }
        if ("APPROVED".equals(session.status)) {
            twoFactorStorage.remove(email);
            return "APPROVED";
        }
        if ("REJECTED".equals(session.status)) {
            twoFactorStorage.remove(email);
            return "FAILED";
        }
        return "PENDING";
    }

    public boolean verify2faFromEmail(String email, int code) {
        TwoFactorSession session = twoFactorStorage.get(email);
        if (session == null || System.currentTimeMillis() > session.expiryTime || !"PENDING".equals(session.status)) {
            return false;
        }
        
        if (session.targetNumber == code) {
            session.status = "APPROVED";
            return true;
        } else {
            session.attemptsLeft--;
            if (session.attemptsLeft <= 0) {
                session.status = "REJECTED";
            }
            return false;
        }
    }

    public Map<String, Object> toggle2fa(String email, boolean enable) {
        User user = userDAO.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setTwoFactorEnabled(enable);
        userDAO.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "2FA has been " + (enable ? "enabled" : "disabled"));
        response.put("twoFactorEnabled", enable);
        return response;
    }

    public AuthResponse resetPassword(String email, String newPassword) {
        Optional<User> userOpt = userDAO.findByEmail(email);
        if (userOpt.isEmpty()) {
            return new AuthResponse(null, null, null, null, null, "User not found");
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userDAO.save(user);

        return new AuthResponse(null, user.getId().toString(), email, user.getName(), user.getRole().name(), "Password reset successfully");
    }

    public AuthResponse generateAndSendOtp(String email) {
        Optional<User> userOpt = userDAO.findByEmail(email);
        if (userOpt.isEmpty()) {
            return new AuthResponse(null, null, null, null, null, "User not found");
        }

        String otp = String.format("%06d", new Random().nextInt(1000000));
        long expiryTime = System.currentTimeMillis() + 5 * 60 * 1000; // 5 minutes
        otpStorage.put(email, new OtpDetails(otp, expiryTime));

        try {
            emailService.sendOtpEmail(email, otp);
        } catch (Exception e) {
            System.err.println("Failed to send OTP email: " + e.getMessage());
        }

        return new AuthResponse(null, null, email, null, null, "OTP sent successfully");
    }

    public boolean verifyOtp(String email, String otp) {
        OtpDetails details = otpStorage.get(email);
        if (details != null) {
            if (System.currentTimeMillis() > details.expiryTime) {
                otpStorage.remove(email); // Expired
                return false;
            }
            if (details.otp.equals(otp)) {
                otpStorage.remove(email); // Valid and verified
                return true;
            }
        }
        return false;
    }
}