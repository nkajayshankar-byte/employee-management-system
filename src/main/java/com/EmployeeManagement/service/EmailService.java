package com.EmployeeManagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;


    @Async
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Password Reset OTP – Employee Management Portal");

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
                    + "<p style='font-size:13px;color:#888;'>This OTP is valid for <strong>10 minutes</strong>."
                    + " Do not share it with anyone.</p>"
                    + "<p style='font-size:13px;color:#888;'>If you did not request this, you can safely ignore this email.</p>"
                    + "</div>"
                    + "<div style='background:#f5f5f5;padding:14px;text-align:center;"
                    + "font-size:12px;color:#aaa;'>Employee Management Portal</div></div>";

            helper.setText(html, true);
            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("[EmailService] Failed to send OTP email to " + toEmail + ": " + e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetSuccessEmail(String toEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Password Changed Successfully – Employee Management Portal");

            String html = "<div style='font-family:Arial,sans-serif;max-width:520px;margin:auto;"
                    + "border:1px solid #e0e0e0;border-radius:8px;overflow:hidden;'>"
                    + "<div style='background:#4caf50;padding:24px;text-align:center;'>"
                    + "<h2 style='color:#fff;margin:0;'>✅ Password Updated</h2></div>"
                    + "<div style='padding:32px;'>"
                    + "<p style='font-size:15px;color:#333;'>Hello,</p>"
                    + "<p style='font-size:15px;color:#333;'>Your password has been changed successfully.</p>"
                    + "<p style='font-size:14px;color:#555;'>You can now log in to the Employee Management Portal"
                    + " using your new password.</p>"
                    + "<p style='font-size:13px;color:#e53935;'><strong>If you did not make this change,"
                    + " please contact your administrator immediately.</strong></p>"
                    + "</div>"
                    + "<div style='background:#f5f5f5;padding:14px;text-align:center;"
                    + "font-size:12px;color:#aaa;'>Employee Management Portal</div></div>";

            helper.setText(html, true);
            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("[EmailService] Failed to send success email to " + toEmail + ": " + e.getMessage());
        }
    }

    @Async
    public void sendApplicationReceivedEmail(String toEmail, String applicantName, String jobTitle) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Application Received – " + jobTitle);

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
                    + "font-size:12px;color:#aaa;'>Employee Management Portal – Careers</div></div>";

            helper.setText(html, true);
            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("[EmailService] Failed to send application email to " + toEmail + ": " + e.getMessage());
        }
    }


    @Async
    public void sendStatusUpdateEmail(String toEmail, String applicantName, String jobTitle, String status) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);

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

            helper.setSubject(subject);

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
                    + "font-size:12px;color:#aaa;'>Employee Management Portal – Careers</div></div>";

            helper.setText(html, true);
            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("[EmailService] Failed to send status email to " + toEmail + ": " + e.getMessage());
        }
    }

    @Async
    public void sendLeaveStatusEmail(String toEmail, String employeeName, String status, String adminEmail, String adminName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, adminName + " (Admin)");
            helper.setReplyTo(adminEmail, adminName);
            helper.setTo(toEmail);
            
            String subject = "Leave Application " + status;
            helper.setSubject(subject);
            
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
                    + "font-size:12px;color:#aaa;'>Employee Management Portal</div></div>";

            helper.setText(html, true);
            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("[EmailService] Failed to send leave status email to " + toEmail + ": " + e.getMessage());
        }
    }
}
