package com.EmployeeManagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LeaveDTO {
    private String id;
    private String employeeId;
    private String employeeName;
    private String leaveType;
    @JsonFormat(pattern = "dd-MM-yy")
    private LocalDate startDate;

    @JsonFormat(pattern = "dd-MM-yy")
    private LocalDate endDate;
    private int numberOfDays;
    private String reason;
    private String status;
    private String approverId;
    private String approverName;
    private String approverComments;
    @JsonFormat(pattern = "dd-MM-yy HH:mm", timezone = "Asia/Kolkata")
    private LocalDateTime approvedAt;

    @JsonFormat(pattern = "dd-MM-yy HH:mm", timezone = "Asia/Kolkata")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "dd-MM-yy HH:mm", timezone = "Asia/Kolkata")
    private LocalDateTime updatedAt;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public int getNumberOfDays() { return numberOfDays; }
    public void setNumberOfDays(int numberOfDays) { this.numberOfDays = numberOfDays; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getApproverId() { return approverId; }
    public void setApproverId(String approverId) { this.approverId = approverId; }
    public String getApproverName() { return approverName; }
    public void setApproverName(String approverName) { this.approverName = approverName; }
    public String getApproverComments() { return approverComments; }
    public void setApproverComments(String approverComments) { this.approverComments = approverComments; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
