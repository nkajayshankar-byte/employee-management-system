package com.EmployeeManagement.controller;

import com.EmployeeManagement.dao.AnnouncementDAO;
import com.EmployeeManagement.entity.Announcement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementDAO announcementDAO;

    @Autowired
    private com.EmployeeManagement.service.AnnouncementScheduler announcementScheduler;

    @PostMapping("/schedule")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> scheduleAnnouncement(@RequestBody Announcement announcement) {
        announcement.setStatus("PENDING");
        int result = announcementDAO.save(announcement);
        if (result > 0) {
            return ResponseEntity.ok("Announcement scheduled successfully.");
        }
        return ResponseEntity.internalServerError().body("Failed to schedule announcement.");
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<java.util.Map<String, String>> generateAnnouncement(@RequestBody java.util.Map<String, String> request) {
        String subject = request.get("subject");
        String audience = request.get("targetAudience");
        
        if (subject == null || subject.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "Subject is required"));
        }
        try {
            String generatedContent = announcementScheduler.generateAnnouncementContent(subject, audience);
            return ResponseEntity.ok(java.util.Map.of("content", generatedContent));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(java.util.Map.of("error", "Failed to generate content: " + e.getMessage()));
        }
    }
}
