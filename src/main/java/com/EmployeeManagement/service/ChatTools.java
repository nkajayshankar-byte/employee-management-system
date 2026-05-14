package com.EmployeeManagement.service;

import com.EmployeeManagement.dao.*;
import com.EmployeeManagement.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChatTools {

    @Autowired
    private LeaveDAO leaveDAO;

    @Autowired
    private AssetDAO assetDAO;

    @Autowired
    private AttendanceDAO attendanceDAO;

    @Autowired
    private JobDAO jobDAO;

    @Autowired
    private ApplicationDAO applicationDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private CompanyDAO companyDAO;

    public record EmployeeIdRequest(Long employeeId) {}
    public record EmailRequest(String email) {}
    public record EmptyRequest() {}

    @Tool(description = "Get the leave balance and status for a specific employee. ID should be a number.")
    public String getLeaveBalance(String employeeId) {
        Long id = parseId(employeeId);
        if (id == null) return "Invalid Employee ID format.";
        List<Leave> leaves = leaveDAO.findByEmployeeId(id);
        if (leaves.isEmpty()) {
            return "No leave records found for this employee. Default balance is 20 days.";
        }
        int usedLeaves = leaves.stream()
                .filter(l -> "APPROVED".equalsIgnoreCase(l.getStatus()))
                .mapToInt(Leave::getNumberOfDays)
                .sum();
        int pendingLeaves = leaves.stream()
                .filter(l -> "PENDING".equalsIgnoreCase(l.getStatus()))
                .mapToInt(Leave::getNumberOfDays)
                .sum();
        return String.format("Total Approved Leaves: %d days, Pending Leaves: %d days. Remaining balance (out of 20): %d days.", 
            usedLeaves, pendingLeaves, 20 - usedLeaves);
    }

    @Tool(description = "Get the list of assets assigned to an employee. ID should be a number.")
    public List<Asset> getAssignedAssets(String employeeId) {
        Long id = parseId(employeeId);
        if (id == null) return List.of();
        return assetDAO.findByEmployeeId(id);
    }

    @Tool(description = "Get the attendance record for an employee on a specific date (YYYY-MM-DD). ID should be a number.")
    public String getAttendance(String employeeId, String date) {
        Long id = parseId(employeeId);
        if (id == null) return "Invalid Employee ID format.";
        LocalDate localDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        return attendanceDAO.findByEmployeeIdAndDate(id, localDate)
                .map(a -> "Status: " + a.getStatus() + ", Check In: " + a.getCheckInTime() + ", Check Out: " + a.getCheckOutTime())
                .orElse("No attendance record found for " + localDate);
    }

    @Tool(description = "READ-ONLY: Get all current job openings available in the company. Does NOT take any arguments.")
    public List<Job> getJobOpenings() {
        return jobDAO.findAll();
    }

    @Tool(description = "Get the status of a job application by email")
    public String getApplicationStatus(String email) {
        User user = userDAO.findByEmail(email).orElse(null);
        if (user == null) return "No user found with email " + email;
        
        List<JobApplication> apps = applicationDAO.findByEmployeeId(user.getId());
        if (apps.isEmpty()) return "No applications found for " + email;
        return apps.stream()
                .map(a -> "Job: " + a.getJobTitle() + ", Status: " + a.getStatus() + ", Applied on: " + a.getAppliedDate())
                .collect(Collectors.joining("\n"));
    }

    @Tool(description = "Get the status of a job application by employee ID. ID should be a number.")
    public String getApplicationStatusByEmployeeId(String employeeId) {
        Long id = parseId(employeeId);
        if (id == null) return "Invalid Employee ID format.";
        List<JobApplication> apps = applicationDAO.findByEmployeeId(id);
        if (apps.isEmpty()) return "No applications found for employee ID " + employeeId;

        return apps.stream()
                .map(a -> "Job: " + a.getJobTitle() + ", Status: " + a.getStatus() + ", Applied on: " + a.getAppliedDate())
                .collect(Collectors.joining("\n"));
    }

    @Tool(description = "Get the company details, mission, vision, values, perks, and contact info")
    public Company getCompanyInfo() {
        return companyDAO.findAll().stream().findFirst().orElse(null);
    }

    private Long parseId(String idStr) {
        if (idStr == null) return null;
        try {
            // Remove any non-numeric characters just in case the AI adds them
            return Long.parseLong(idStr.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
