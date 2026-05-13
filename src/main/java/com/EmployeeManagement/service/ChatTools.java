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

    @Tool(description = "Get the leave balance and status for a specific employee")
    public String getLeaveBalance(Long employeeId) {
        List<Leave> leaves = leaveDAO.findByEmployeeId(employeeId);
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

    @Tool(description = "Get the list of assets assigned to an employee")
    public List<Asset> getAssignedAssets(Long employeeId) {
        return assetDAO.findByEmployeeId(employeeId);
    }

    @Tool(description = "Get the attendance record for an employee on a specific date (YYYY-MM-DD). If no date is provided or user says 'today', the current date is used.")
    public String getAttendance(Long employeeId, String date) {
        LocalDate localDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        return attendanceDAO.findByEmployeeIdAndDate(employeeId, localDate)
                .map(a -> "Status: " + a.getStatus() + ", Check In: " + a.getCheckInTime() + ", Check Out: " + a.getCheckOutTime())
                .orElse("No attendance record found for " + localDate);
    }

    public record AttendanceRequest(Long employeeId, String date) {}

    @Tool(description = "Get all current job openings in the company")
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

    @Tool(description = "Get the status of a job application by employee ID")
    public String getApplicationStatusByEmployeeId(Long employeeId) {
        List<JobApplication> apps = applicationDAO.findByEmployeeId(employeeId);
        if (apps.isEmpty()) return "No applications found for employee ID " + employeeId;

        return apps.stream()
                .map(a -> "Job: " + a.getJobTitle() + ", Status: " + a.getStatus() + ", Applied on: " + a.getAppliedDate())
                .collect(Collectors.joining("\n"));
    }

    @Tool(description = "Get the company details, mission, vision, values, perks, and contact info")
    public Company getCompanyInfo() {
        return companyDAO.findAll().stream().findFirst().orElse(null);
    }
}
