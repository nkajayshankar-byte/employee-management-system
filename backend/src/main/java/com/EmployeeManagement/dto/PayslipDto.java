package com.EmployeeManagement.dto;

public class PayslipDto {
    private Long id;
    private Long employeeId;
    private Integer month;
    private Integer year;
    private Integer totalDays;
    private Integer paidDays;
    private Double grossPay;
    private Double totalDeductions;
    private Double lopAmount;
    private Double netPay;
    private String status;
    private String pdfUrl;
    
    // Additional fields for display
    private String employeeName;
    private String employeeEmail;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getTotalDays() { return totalDays; }
    public void setTotalDays(Integer totalDays) { this.totalDays = totalDays; }

    public Integer getPaidDays() { return paidDays; }
    public void setPaidDays(Integer paidDays) { this.paidDays = paidDays; }

    public Double getGrossPay() { return grossPay; }
    public void setGrossPay(Double grossPay) { this.grossPay = grossPay; }

    public Double getTotalDeductions() { return totalDeductions; }
    public void setTotalDeductions(Double totalDeductions) { this.totalDeductions = totalDeductions; }

    public Double getNetPay() { return netPay; }
    public void setNetPay(Double netPay) { this.netPay = netPay; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEmployeeEmail() { return employeeEmail; }
    public void setEmployeeEmail(String employeeEmail) { this.employeeEmail = employeeEmail; }

    public Double getLopAmount() { return lopAmount; }
    public void setLopAmount(Double lopAmount) { this.lopAmount = lopAmount; }
}
