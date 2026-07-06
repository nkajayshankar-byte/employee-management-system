package com.EmployeeManagement.dto;

public class SalaryStructureDto {
    private Long id;
    private Long employeeId;
    private Double baseSalary;
    private Double hra;
    private Double otherAllowances;
    private Double taxDeductions;
    private Double providentFund;
    private Double netSalary;
    private String accountNumber;

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

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
}
