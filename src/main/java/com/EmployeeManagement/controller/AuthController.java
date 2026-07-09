package com.EmployeeManagement.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.EmployeeManagement.dto.AuthRequest;
import com.EmployeeManagement.dto.AuthResponse;
import com.EmployeeManagement.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody AuthRequest request) {

        System.out.println("===== SIGNUP CONTROLLER HIT =====");

        try {

            AuthResponse response = authService.signup(request);

            System.out.println("===== SIGNUP COMPLETED =====");

            if (response.getToken() == null) {
                if ("INVALID_ADMIN_KEY".equals(response.getMessage())) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("message", "Invalid Admin Secret Key"));
                }

                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", response.getMessage()));
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            System.out.println("===== SIGNUP EXCEPTION =====");
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", e.getClass().getSimpleName(),
                            "message", e.getMessage()
                    ));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        if (request.getEmail() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email and Password are required"));
        }
        try {
            Map<String, Object> response = authService.loginUser(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            if ("User not found".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", e.getMessage()));
            }
            if ("Invalid password".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> req) {

        String email = req.get("email");
        String newPassword = req.get("newPassword");

        if (email == null || newPassword == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email and newPassword are required"));
        }

        AuthResponse response = authService.resetPassword(email, newPassword);

        if (response.getMessage().equalsIgnoreCase("User not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }
        
        AuthResponse response = authService.generateAndSendOtp(email);
        
        if (response.getMessage().equalsIgnoreCase("User not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String otp = req.get("otp");
        
        if (email == null || otp == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email and OTP are required"));
        }
        
        boolean isValid = authService.verifyOtp(email, otp);
        
        if (!isValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid or expired OTP"));
        }
        
        return ResponseEntity.ok(Map.of("message", "OTP verified successfully"));
    }

    @org.springframework.web.bind.annotation.GetMapping("/2fa-status")
    public ResponseEntity<?> check2faStatus(@org.springframework.web.bind.annotation.RequestParam String email) {
        String status = authService.check2faStatus(email);
        if ("APPROVED".equals(status)) {
            return ResponseEntity.ok(authService.complete2faLogin(email));
        }
        return ResponseEntity.ok(Map.of("status", status));
    }

    @org.springframework.web.bind.annotation.GetMapping("/verify-2fa-email")
    public ResponseEntity<String> verify2faEmail(@org.springframework.web.bind.annotation.RequestParam String email, @org.springframework.web.bind.annotation.RequestParam int code) {
        boolean isValid = authService.verify2faFromEmail(email, code);
        
        String htmlResponse;
        if (isValid) {
            htmlResponse = "<html><body style='font-family:Arial;text-align:center;margin-top:50px;color:#4caf50;'>"
                         + "<h2>✅ Login Approved!</h2>"
                         + "<p>You can now return to your original tab to use the app.</p>"
                         + "<script>setTimeout(()=>window.close(), 3000);</script>"
                         + "</body></html>";
        } else {
            htmlResponse = "<html><body style='font-family:Arial;text-align:center;margin-top:50px;color:#f44336;'>"
                         + "<h2>❌ Invalid or Expired Request</h2>"
                         + "<p>The number you tapped was incorrect or the request expired.</p>"
                         + "</body></html>";
        }
        
        return ResponseEntity.status(isValid ? HttpStatus.OK : HttpStatus.UNAUTHORIZED)
                .contentType(org.springframework.http.MediaType.TEXT_HTML)
                .body(htmlResponse);
    }

    @PostMapping("/toggle-2fa")
    public ResponseEntity<?> toggle2fa(@RequestBody Map<String, Object> req) {
        String email = (String) req.get("email");
        Boolean enable = (Boolean) req.get("enable");
        
        if (email == null || enable == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email and enable status are required"));
        }

        try {
            Map<String, Object> response = authService.toggle2fa(email, enable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }
}