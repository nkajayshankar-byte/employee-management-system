package com.EmployeeManagement.mapper;

import com.EmployeeManagement.dto.JobApplicationDTO;
import com.EmployeeManagement.entity.JobApplication;
import org.springframework.stereotype.Component;

@Component
public class ApplicationMapper {

    public JobApplicationDTO toDTO(JobApplication app) {
        if (app == null) return null;

        JobApplicationDTO dto = new JobApplicationDTO();
        dto.setId(app.getId());
        dto.setJobId(app.getJobId());
        dto.setEmployeeId(app.getEmployeeId());
        dto.setEmployeeName(app.getEmployeeName());
        dto.setEmployeeEmail(app.getEmployeeEmail());
        dto.setResumeUrl(app.getResumeUrl());
        dto.setStatus(app.getStatus());
        dto.setAppliedDate(app.getAppliedDate());

        return dto;
    }

    public JobApplication toEntity(JobApplicationDTO dto) {
        if (dto == null) return null;

        JobApplication app = new JobApplication();
        app.setId(dto.getId());
        app.setJobId(dto.getJobId());
        app.setEmployeeId(dto.getEmployeeId());
        app.setEmployeeName(dto.getEmployeeName());
        app.setEmployeeEmail(dto.getEmployeeEmail());
        app.setResumeUrl(dto.getResumeUrl());
        app.setStatus(dto.getStatus());
        app.setAppliedDate(dto.getAppliedDate());

        return app;
    }
}
