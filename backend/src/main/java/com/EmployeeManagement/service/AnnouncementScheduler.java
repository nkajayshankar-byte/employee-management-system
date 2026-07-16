package com.EmployeeManagement.service;

import com.EmployeeManagement.dao.AnnouncementDAO;
import com.EmployeeManagement.dao.UserDAO;
import com.EmployeeManagement.entity.Announcement;
import com.EmployeeManagement.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnnouncementScheduler {

    @Autowired
    private AnnouncementDAO announcementDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private EmailService emailService;

    @Autowired
    private org.springframework.ai.chat.model.ChatModel chatModel;

    @Scheduled(fixedRate = 60000) // Run every minute
    public void processAnnouncements() {
        List<Announcement> pendingAnnouncements = announcementDAO.findPendingAnnouncementsDue();

        for (Announcement announcement : pendingAnnouncements) {
            try {
                List<User> users = userDAO.findAll();
                
                // Filter based on target audience
                List<String> bccList = users.stream()
                        .filter(u -> matchesAudience(u, announcement.getTargetAudience()))
                        .map(User::getEmail)
                        .collect(Collectors.toList());

                if (!bccList.isEmpty()) {
                    // Wrap the announcement content in a professional HTML template
                    String htmlContent = "<div style='font-family:Arial,sans-serif;max-width:600px;margin:auto;"
                            + "border:1px solid #e0e0e0;border-radius:8px;overflow:hidden;'>"
                            + "<div style='background:#4e73df;padding:24px;text-align:center;'>"
                            + "<h2 style='color:#fff;margin:0;'>📢 Company Announcement</h2></div>"
                            + "<div style='padding:32px; white-space: pre-wrap; color: #333; font-size: 15px;'>"
                            + announcement.getContent()
                            + "</div>"
                            + "<div style='background:#f8fafc;padding:14px;text-align:center;"
                            + "font-size:12px;color:#aaa;'>Phoenix</div></div>";

                    emailService.sendAnnouncementEmails(bccList, announcement.getSubject(), htmlContent);
                }

                announcementDAO.updateStatus(announcement.getId(), "SENT");
            } catch (Exception e) {
                e.printStackTrace();
                announcementDAO.updateStatus(announcement.getId(), "FAILED");
            }
        }
    }

    private boolean matchesAudience(User user, String targetAudience) {
        if ("Organization".equalsIgnoreCase(targetAudience)) {
            return true;
        } else if ("Admins".equalsIgnoreCase(targetAudience)) {
            return "ADMIN".equalsIgnoreCase(user.getRole().name());
        } else if ("Employees".equalsIgnoreCase(targetAudience)) {
            return "EMPLOYEE".equalsIgnoreCase(user.getRole().name());
        } else if ("Users".equalsIgnoreCase(targetAudience)) {
            return "USER".equalsIgnoreCase(user.getRole().name());
        }
        return true;
    }

    public String generateAnnouncementContent(String subject, String targetAudience) {
        String greeting = "Team";
        if (targetAudience != null && !targetAudience.trim().isEmpty()) {
            if (targetAudience.equalsIgnoreCase("Organization")) {
                greeting = "Team"; // Represents the whole organization
            } else {
                greeting = targetAudience; // e.g., Admins, Users, Employees
            }
        }
        
        String promptText = "Generate a professional company announcement based on the following subject: '" + subject + "'. " +
                            "The announcement must be 150-250 words in professional HR language. " +
                            "It must start exactly with 'Dear " + greeting + ",' and \"It must end with these two lines exactly:\\n\\nRegards,\\nPhoenix Management\"";
        return chatModel.call(promptText);
    }
}
