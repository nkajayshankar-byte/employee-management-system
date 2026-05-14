package com.EmployeeManagement.dto;

import lombok.Data;
import java.util.List;

@Data
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
public class ResumeAnalysisResponse {
    private int matchPercentage;
    private List<String> missingSkills;
    private List<String> strengths;
    private String summary;
    private String extractedSkills;
    private String extractedExperience;
    private String extractedEducation;
}
