package com.EmployeeManagement.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class SalaryStructure {
    private Long id;
    private Long employeeId;
    private Double baseSalary;
    private Double hra;
    private Double otherAllowances;
    private Double taxDeductions;
    private Double providentFund;
    private Double netSalary;
    private String accountNumber;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "dd-MM-yy HH:mm", timezone = "Asia/Kolkata")
    private LocalDateTime updatedAt;

    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        calculateNetSalary();
    }

    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        calculateNetSalary();
    }

    public void calculateNetSalary() {
        double gross = (baseSalary != null ? baseSalary : 0) + 
                       (hra != null ? hra : 0) + 
                       (otherAllowances != null ? otherAllowances : 0);
        double deductions = (taxDeductions != null ? taxDeductions : 0) + 
                            (providentFund != null ? providentFund : 0);
        this.netSalary = gross - deductions;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public Double getBaseSalary() { return baseSalary; }
    public void setBaseSalary(Double baseSalary) { this.baseSalary = baseSalary; }

    public Double getHra() { return hra; }
    public void setHra(Double hra) { this.hra = hra; }

    public Double getOtherAllowances() { return otherAllowances; }
    public void setOtherAllowances(Double otherAllowances) { this.otherAllowances = otherAllowances; }

    public Double getTaxDeductions() { return taxDeductions; }
    public void setTaxDeductions(Double taxDeductions) { this.taxDeductions = taxDeductions; }

    public Double getProvidentFund() { return providentFund; }
    public void setProvidentFund(Double providentFund) { this.providentFund = providentFund; }

    public Double getNetSalary() { return netSalary; }
    public void setNetSalary(Double netSalary) { this.netSalary = netSalary; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
}
