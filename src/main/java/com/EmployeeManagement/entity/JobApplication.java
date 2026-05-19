package com.EmployeeManagement.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobApplication {
    private Long id;
    private Long jobId;
    private Long employeeId;
    // Fields stored in DB
    private String employeeName;
    private String employeeEmail;
    private String employeePhone;

    // Virtual fields
    private String jobTitle;
    private Boolean jobActive;
    private String resumeUrl;
    private String status = "PENDING";
    @JsonFormat(pattern = "dd-MM-yy HH:mm", timezone = "Asia/Kolkata")
    private LocalDateTime appliedDate = LocalDateTime.now();

    // AI Screening results
    private Integer matchPercentage;
    private String missingSkills;
    private String strengths;
    private String summary;
    private String extractedSkills;
    private String extractedExperience;
    private String extractedEducation;
}
