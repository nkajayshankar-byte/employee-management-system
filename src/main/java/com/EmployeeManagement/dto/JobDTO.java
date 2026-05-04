package com.EmployeeManagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;

public class JobDTO {
    private String id;
    private String title;
    private String department;
    private String location;
    private String type;
    private String description;
    private double minSalary;
    private double maxSalary;
    private List<String> requiredSkills;
    @JsonFormat(pattern = "dd-MM-yy HH:mm", timezone = "Asia/Kolkata")
    private LocalDateTime postedDate;
    private String contactEmail;
    private String keyResponsibilities;
    private Boolean isActive;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getMinSalary() { return minSalary; }
    public void setMinSalary(double minSalary) { this.minSalary = minSalary; }
    public double getMaxSalary() { return maxSalary; }
    public void setMaxSalary(double maxSalary) { this.maxSalary = maxSalary; }
    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills; }
    public LocalDateTime getPostedDate() { return postedDate; }
    public void setPostedDate(LocalDateTime postedDate) { this.postedDate = postedDate; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getKeyResponsibilities() { return keyResponsibilities; }
    public void setKeyResponsibilities(String keyResponsibilities) { this.keyResponsibilities = keyResponsibilities; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
