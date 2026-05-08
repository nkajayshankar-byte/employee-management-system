package com.EmployeeManagement.dao;

import com.EmployeeManagement.entity.JobApplication;
import java.util.List;
import java.util.Optional;

public interface ApplicationDAO {
    JobApplication save(JobApplication application);
    Optional<JobApplication> findById(Long id);
    List<JobApplication> findAll();
    List<JobApplication> findByJobId(Long jobId);
    List<JobApplication> findByEmployeeId(Long employeeId);
    boolean existsByJobIdAndEmployeeId(Long jobId, Long employeeId);
    void deleteById(Long id);
    void deleteByJobId(Long jobId);
    void deleteByEmployeeId(Long employeeId);
}
