package com.EmployeeManagement.mapper;

import com.EmployeeManagement.dto.JobDTO;
import com.EmployeeManagement.entity.Job;
import org.springframework.stereotype.Component;

@Component
public class JobMapper {

    public JobDTO toDTO(Job job) {
        if (job == null) return null;

        JobDTO dto = new JobDTO();
        dto.setId(job.getId());
        dto.setTitle(job.getTitle());
        dto.setDepartment(job.getDepartment());
        dto.setLocation(job.getLocation());
        dto.setType(job.getType());
        dto.setDescription(job.getDescription());
        dto.setMinSalary(job.getMinSalary());
        dto.setMaxSalary(job.getMaxSalary());
        dto.setRequiredSkills(job.getRequiredSkills());
        dto.setKeyResponsibilities(job.getKeyResponsibilities());
        dto.setPostedDate(job.getCreatedAt());
        dto.setIsActive(job.getIsActive());

        return dto;
    }

    public Job toEntity(JobDTO dto) {
        if (dto == null) return null;

        Job job = new Job();
        job.setId(dto.getId());
        job.setTitle(dto.getTitle());
        job.setDepartment(dto.getDepartment());
        job.setLocation(dto.getLocation());
        job.setType(dto.getType());
        job.setDescription(dto.getDescription());
        job.setMinSalary((int)dto.getMinSalary());
        job.setMaxSalary((int)dto.getMaxSalary());
        job.setRequiredSkills(dto.getRequiredSkills());
        job.setKeyResponsibilities(dto.getKeyResponsibilities());
        job.setCreatedAt(dto.getPostedDate());
        if (dto.getIsActive() != null) {
            job.setIsActive(dto.getIsActive());
        }

        return job;
    }
}
