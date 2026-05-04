package com.EmployeeManagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;


public class AssetDTO {
    private String id;
    private String employeeId;
    private String employeeName;
    private String assetName;
    private String assetType;
    private String serialNumber;
    private String status;
    @JsonFormat(pattern = "dd-MM-yy HH:mm", timezone = "Asia/Kolkata")
    private LocalDateTime assignedDate;

    @JsonFormat(pattern = "dd-MM-yy HH:mm", timezone = "Asia/Kolkata")
    private LocalDateTime returnDate;
    private String conditions;
    private String description;
    private String remarks;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getEmployeeId() {
		return employeeId;
	}
	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}
	public String getEmployeeName() {
		return employeeName;
	}
	public void setEmployeeName(String employeeName) {
		this.employeeName = employeeName;
	}
	public String getAssetName() {
		return assetName;
	}
	public void setAssetName(String assetName) {
		this.assetName = assetName;
	}
	public String getAssetType() {
		return assetType;
	}
	public void setAssetType(String assetType) {
		this.assetType = assetType;
	}
	public String getSerialNumber() {
		return serialNumber;
	}
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public LocalDateTime getAssignedDate() {
		return assignedDate;
	}
	public void setAssignedDate(LocalDateTime assignedDate) {
		this.assignedDate = assignedDate;
	}
	public LocalDateTime getReturnDate() {
		return returnDate;
	}
	public void setReturnDate(LocalDateTime returnDate) {
		this.returnDate = returnDate;
	}
	public String getConditions() {
		return conditions;
	}
	public void setConditions(String conditions) {
		this.conditions = conditions;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}