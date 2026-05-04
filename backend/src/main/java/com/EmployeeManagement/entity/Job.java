package com.EmployeeManagement.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Job {
    private String id;
    private String title;
    private String department;
    private String location;
    private String type;
    private String description;
    private String keyResponsibilities;
    
    private int minSalary;
    private int maxSalary;
    private List<String> requiredSkills = new ArrayList<>();
    @JsonFormat(pattern = "dd-MM-yy HH:mm", timezone = "Asia/Kolkata")
    private LocalDateTime createdAt = LocalDateTime.now();
    private Boolean isActive = true;

    public Job() {}

    public Job(String id, String title, String department, String location, String type, String description, 
               int minSalary, int maxSalary, List<String> requiredSkills, LocalDateTime createdAt, String keyResponsibilities) {
        this.id = id;
        this.title = title;
        this.department = department;
        this.location = location;
        this.type = type;
        this.description = description;
        this.minSalary = minSalary;
        this.maxSalary = maxSalary;
        this.requiredSkills = requiredSkills;
        this.createdAt = createdAt;
        this.keyResponsibilities=keyResponsibilities;
    }

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
    public String getKeyResponsibilities() { return keyResponsibilities; }
    public void setKeyResponsibilities(String keyResponsibilities) { this.keyResponsibilities = keyResponsibilities; }
    public int getMinSalary() { return minSalary; }
    public void setMinSalary(int minSalary) { this.minSalary = minSalary; }
    public int getMaxSalary() { return maxSalary; }
    public void setMaxSalary(int maxSalary) { this.maxSalary = maxSalary; }
    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
