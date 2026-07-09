package com.EmployeeManagement.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Service
public class EmailService {

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.from.email}")
    private String fromEmail;
    
    @Value("${brevo.from.name:Phoenix}")
    private String fromName;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final RestTemplate restTemplate;
    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    public EmailService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5 seconds
        factory.setReadTimeout(10000); // 10 seconds
        this.restTemplate = new RestTemplate(factory);
    }

    @Async
    public void sendWelcomeEmail(String toEmail, String name) {
        String subject = "Welcome to Phoenix!";
        String html = "<div style='font-family:Arial,sans-serif;max-width:520px;margin:auto;"
                + "border:1px solid #e0e0e0;border-radius:8px;overflow:hidden;'>"
                + "<div style='background:#667eea;padding:24px;text-align:center;'>"
                + "<h2 style='color:#fff;margin:0;'>Welcome Aboard! 🚀</h2></div>"
                + "<div style='padding:32px;'>"
                + "<p style='font-size:15px;color:#333;'>Hello <strong>" + name + "</strong>,</p>"
                + "<p style='font-size:15px;color:#333;'>We are thrilled to have you join the Phoenix.</p>"
                + "<p style='font-size:14px;color:#555;'>Our platform helps you manage your attendance, leave applications, assets, and career opportunities all in one place.</p>"
                + "<p style='font-size:14px;color:#555;'>Log in now to explore your dashboard and get started.</p>"
                + "</div>"
                + "<div style='background:#f5f5f5;padding:14px;text-align:center;"
                + "font-size:12px;color:#aaa;'>Phoenix</div></div>";

        sendEmail(toEmail, subject, html);
    }

    /**
     * Helper method to send email via Brevo API
     */
    private void sendEmail(String toEmail, String subject, String htmlContent) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            Map<String, Object> body = new HashMap<>();
            
            Map<String, String> sender = new HashMap<>();
            sender.put("email", fromEmail);
            sender.put("name", fromName);
            body.put("sender", sender);

            Map<String, String> to = new HashMap<>();
            to.put("email", toEmail);
            body.put("to", List.of(to));

            body.put("subject", subject);
            body.put("htmlContent", htmlContent);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(BREVO_API_URL, HttpMethod.POST, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("[EmailService] Email sent successfully via Brevo to {}", toEmail);
            } else {
                logger.error("[EmailService] Failed to send email via Brevo. Status Code: {}", response.getStatusCode());
                logger.error("[EmailService] Response Body: {}", response.getBody());
            }
        } catch (Exception ex) {
            logger.error("[EmailService] Exception while sending email via Brevo to {}: {}", toEmail, ex.getMessage(), ex);
        }
    }

    @Async
    public void sendOtpEmail(String toEmail, String otp) {
        String subject = "Password Reset OTP – Phoenix";
        String html = "<div style='font-family:Arial,sans-serif;max-width:520px;margin:auto;"
                + "border:1px solid #e0e0e0;border-radius:8px;overflow:hidden;'>"
                + "<div style='background:#667eea;padding:24px;text-align:center;'>"
                + "<h2 style='color:#fff;margin:0;'>Password Reset Request</h2></div>"
                + "<div style='padding:32px;'>"
                + "<p style='font-size:15px;color:#333;'>Hello,</p>"
                + "<p style='font-size:15px;color:#333;'>We received a request to reset your password."
                + " Use the OTP below to proceed:</p>"
                + "<div style='text-align:center;margin:28px 0;'>"
                + "<span style='font-size:36px;font-weight:bold;letter-spacing:10px;"
                + "color:#667eea;background:#f0f4ff;padding:14px 28px;border-radius:8px;'>"
                + otp + "</span></div>"
                + "<p style='font-size:13px;color:#888;'>This OTP is valid for <strong>5 minutes</strong>."
                + " Do not share it with anyone.</p>"
                + "<p style='font-size:13px;color:#888;'>If you did not request this, you can safely ignore this email.</p>"
                + "</div>"
                + "<div style='background:#f5f5f5;padding:14px;text-align:center;"
                + "font-size:12px;color:#aaa;'>Phoenix</div></div>";

        sendEmail(toEmail, subject, html);
    }

    @Async
    public void sendPasswordResetSuccessEmail(String toEmail) {
        String subject = "Password Changed Successfully – Phoenix";
        String html = "<div style='font-family:Arial,sans-serif;max-width:520px;margin:auto;"
                + "border:1px solid #e0e0e0;border-radius:8px;overflow:hidden;'>"
                + "<div style='background:#4caf50;padding:24px;text-align:center;'>"
                + "<h2 style='color:#fff;margin:0;'>✅ Password Updated</h2></div>"
                + "<div style='padding:32px;'>"
                + "<p style='font-size:15px;color:#333;'>Hello,</p>"
                + "<p style='font-size:15px;color:#333;'>Your password has been changed successfully.</p>"
                + "<p style='font-size:14px;color:#555;'>You can now log in to the Phoenix"
                + " using your new password.</p>"
                + "<p style='font-size:13px;color:#e53935;'><strong>If you did not make this change,"
                + " please contact your administrator immediately.</strong></p>"
                + "</div>"
                + "<div style='background:#f5f5f5;padding:14px;text-align:center;"
                + "font-size:12px;color:#aaa;'>Phoenix</div></div>";

        sendEmail(toEmail, subject, html);
    }

    @Async
    public void sendApplicationReceivedEmail(String toEmail, String applicantName, String jobTitle) {
        String subject = "Application Received – " + jobTitle;
        String html = "<div style='font-family:Arial,sans-serif;max-width:520px;margin:auto;"
                + "border:1px solid #e0e0e0;border-radius:8px;overflow:hidden;'>"
                + "<div style='background:#667eea;padding:24px;text-align:center;'>"
                + "<h2 style='color:#fff;margin:0;'>Application Submitted 🎉</h2></div>"
                + "<div style='padding:32px;'>"
                + "<p style='font-size:15px;color:#333;'>Dear <strong>" + applicantName + "</strong>,</p>"
                + "<p style='font-size:15px;color:#333;'>Thank you for applying for the position of"
                + " <strong>" + jobTitle + "</strong>.</p>"
                + "<p style='font-size:14px;color:#555;'>Your application has been successfully submitted"
                + " and is currently <strong>under review</strong>.</p>"
                + "<p style='font-size:14px;color:#555;'>Our team will reach out to you with updates"
                + " regarding your application status. We appreciate your interest!</p>"
                + "</div>"
                + "<div style='background:#f5f5f5;padding:14px;text-align:center;"
                + "font-size:12px;color:#aaa;'>Phoenix – Careers</div></div>";

        sendEmail(toEmail, subject, html);
    }


    @Async
    public void sendStatusUpdateEmail(String toEmail, String applicantName, String jobTitle, String status) {
        String subject;
        String bodyMessage;
        String headerColor;
        String headerTitle;

        switch (status.toUpperCase()) {
            case "PENDING":
                subject      = "Application Status Update – Under Review";
                headerColor  = "#ff9800";
                headerTitle  = "⏳ Application Under Review";
                bodyMessage  = "Your application is currently under review. We will notify you of any updates.";
                break;
            case "SHORTLISTED":
                subject      = "Great News! You've Been Shortlisted";
                headerColor  = "#2196f3";
                headerTitle  = "🌟 You Have Been Shortlisted";
                bodyMessage  = "Congratulations! You have been <strong>shortlisted</strong> for the position."
                             + " Our team will be in touch shortly with further details.";
                break;
            case "HIRED":
                subject      = "Congratulations! You Have Been Selected";
                headerColor  = "#4caf50";
                headerTitle  = "🎉 Congratulations! You're Hired";
                bodyMessage  = "Congratulations! You have been <strong>selected</strong> for the position of"
                             + " <strong>" + jobTitle + "</strong>."
                             + " Welcome to the team! Our HR team will contact you with the next steps.";
                break;
            case "REJECTED":
                subject      = "Update on Your Job Application";
                headerColor  = "#f44336";
                headerTitle  = "Application Status Update";
                bodyMessage  = "We regret to inform you that your application for <strong>" + jobTitle + "</strong>"
                             + " was not selected at this time."
                             + " We encourage you to apply for future openings that match your profile.";
                break;
            default:
                subject      = "Application Status Update";
                headerColor  = "#607d8b";
                headerTitle  = "Application Status Update";
                bodyMessage  = "Your application status has been updated to: <strong>" + status + "</strong>.";
        }

        String html = "<div style='font-family:Arial,sans-serif;max-width:520px;margin:auto;"
                + "border:1px solid #e0e0e0;border-radius:8px;overflow:hidden;'>"
                + "<div style='background:" + headerColor + ";padding:24px;text-align:center;'>"
                + "<h2 style='color:#fff;margin:0;'>" + headerTitle + "</h2></div>"
                + "<div style='padding:32px;'>"
                + "<p style='font-size:15px;color:#333;'>Dear <strong>" + applicantName + "</strong>,</p>"
                + "<p style='font-size:15px;color:#333;'>Regarding your application for"
                + " <strong>" + jobTitle + "</strong>:</p>"
                + "<p style='font-size:15px;color:#555;'>" + bodyMessage + "</p>"
                + "<p style='font-size:13px;color:#888;margin-top:24px;'>Thank you for your interest"
                + " in our organization.</p>"
                + "</div>"
                + "<div style='background:#f5f5f5;padding:14px;text-align:center;"
                + "font-size:12px;color:#aaa;'>Phoenix – Careers</div></div>";

        sendEmail(toEmail, subject, html);
    }

    @Async
    public void sendLeaveStatusEmail(String toEmail, String employeeName, String status, String adminEmail, String adminName) {
        String subject = "Leave Application " + status;
        String color = status.equalsIgnoreCase("APPROVED") ? "#4caf50" : "#f44336";
        String title = status.equalsIgnoreCase("APPROVED") ? "Leave Approved ✅" : "Leave Rejected ❌";
        
        String html = "<div style='font-family:Arial,sans-serif;max-width:520px;margin:auto;"
                + "border:1px solid #e0e0e0;border-radius:8px;overflow:hidden;'>"
                + "<div style='background:" + color + ";padding:24px;text-align:center;'>"
                + "<h2 style='color:#fff;margin:0;'>" + title + "</h2></div>"
                + "<div style='padding:32px;'>"
                + "<p style='font-size:15px;color:#333;'>Dear <strong>" + employeeName + "</strong>,</p>"
                + "<p style='font-size:15px;color:#333;'>Your leave application has been <strong>" + status + "</strong> by " + adminName + ".</p>"
                + "</div>"
                + "<div style='background:#f5f5f5;padding:14px;text-align:center;"
                + "font-size:12px;color:#aaa;'>Phoenix</div></div>";

        sendEmail(toEmail, subject, html);
    }
    @Async
    public void send2faEmail(String toEmail, int targetNumber, java.util.List<Integer> allNumbers) {
        logger.info("[EmailService] Starting to send 2FA email to {}", toEmail);
        String subject = "Your 2FA Login Code – Phoenix";
        
        StringBuilder buttonsHtml = new StringBuilder();
        for (Integer num : allNumbers) {
            String url = baseUrl + "/api/auth/verify-2fa-email?email=" + toEmail + "&code=" + num;
            buttonsHtml.append("<a href='").append(url).append("' style='display:inline-block; padding:15px 30px; margin:10px; font-size:24px; font-weight:bold; color:#ffffff; background-color:#667eea; text-decoration:none; border-radius:8px;'>")
                       .append(String.format("%02d", num)).append("</a>");
        }

        String html = "<div style='font-family:Arial,sans-serif;max-width:520px;margin:auto;border:1px solid #e0e0e0;border-radius:8px;overflow:hidden;'>"
                + "<div style='background:#667eea;padding:24px;text-align:center;'>"
                + "<h2 style='color:#fff;margin:0;'>Sign-in Request</h2></div>"
                + "<div style='padding:32px; text-align:center;'>"
                + "<p style='font-size:16px;color:#333;'>We received a request to log in to your account.</p>"
                + "<p style='font-size:16px;color:#333;margin-bottom:24px;'><strong>Tap the number below that matches what is shown on your screen:</strong></p>"
                + "<div>" + buttonsHtml.toString() + "</div>"
                + "<p style='font-size:13px;color:#888;margin-top:32px;'>This request expires in 2 minutes.</p>"
                + "</div>"
                + "<div style='background:#f5f5f5;padding:14px;text-align:center;font-size:12px;color:#aaa;'>Phoenix</div></div>";

        sendEmail(toEmail, subject, html);
    }
}
