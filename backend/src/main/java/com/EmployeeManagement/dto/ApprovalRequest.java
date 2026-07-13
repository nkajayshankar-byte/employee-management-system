package com.EmployeeManagement.dto;

public class ApprovalRequest {
    private String approverName;
    private String comments;

    public String getApproverName() { return approverName; }
    public void setApproverName(String approverName) { this.approverName = approverName; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}
