package com.EmployeeManagement.dao;

import com.EmployeeManagement.entity.JobApplication;
import java.util.List;
import java.util.Optional;

public interface ApplicationDAO {
    JobApplication save(JobApplication application);
    Optional<JobApplication> findById(String id);
    List<JobApplication> findAll();
    List<JobApplication> findByJobId(String jobId);
    List<JobApplication> findByEmployeeId(String employeeId);
    boolean existsByJobIdAndEmployeeId(String jobId, String employeeId);
    void deleteById(String id);
    void deleteByJobId(String jobId);
    void deleteByEmployeeId(String employeeId);
}
