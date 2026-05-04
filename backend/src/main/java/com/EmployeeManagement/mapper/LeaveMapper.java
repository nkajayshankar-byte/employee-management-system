package com.EmployeeManagement.mapper;

import com.EmployeeManagement.dto.LeaveDTO;
import com.EmployeeManagement.entity.Leave;
import org.springframework.stereotype.Component;

@Component
public class LeaveMapper {

    public LeaveDTO toDTO(Leave leave) {
        if (leave == null) return null;

        LeaveDTO dto = new LeaveDTO();
        dto.setId(leave.getId());
        dto.setEmployeeId(leave.getEmployeeId());
        dto.setEmployeeName(leave.getEmployeeName());
        dto.setLeaveType(leave.getLeaveType());
        dto.setStartDate(leave.getStartDate());
        dto.setEndDate(leave.getEndDate());
        dto.setNumberOfDays(leave.getNumberOfDays());
        dto.setReason(leave.getReason());
        dto.setStatus(leave.getStatus());
        dto.setApproverId(leave.getApproverId());
        dto.setApproverName(leave.getApproverName());
        dto.setApproverComments(leave.getApproverComments());
        dto.setApprovedAt(leave.getApprovedAt());
        dto.setCreatedAt(leave.getCreatedAt());
        dto.setUpdatedAt(leave.getUpdatedAt());

        return dto;
    }

    public Leave toEntity(LeaveDTO dto) {
        if (dto == null) return null;

        Leave leave = new Leave();
        leave.setId(dto.getId());
        leave.setEmployeeId(dto.getEmployeeId());
        leave.setEmployeeName(dto.getEmployeeName());
        leave.setLeaveType(dto.getLeaveType());
        leave.setStartDate(dto.getStartDate());
        leave.setEndDate(dto.getEndDate());
        leave.setNumberOfDays(dto.getNumberOfDays());
        leave.setReason(dto.getReason());
        leave.setStatus(dto.getStatus());
        leave.setApproverId(dto.getApproverId());
        leave.setApproverName(dto.getApproverName());
        leave.setApproverComments(dto.getApproverComments());
        leave.setApprovedAt(dto.getApprovedAt());
        leave.setCreatedAt(dto.getCreatedAt());
        leave.setUpdatedAt(dto.getUpdatedAt());

        return leave;
    }
}
