package com.EmployeeManagement.dao;

import com.EmployeeManagement.entity.Job;
import java.util.List;
import java.util.Optional;

public interface JobDAO {
    Job save(Job job);
    Optional<Job> findById(Long id);
    List<Job> findAll();
    void deleteById(Long id);
    String findHiredJobTitleByEmployeeId(Long employeeId);
}
