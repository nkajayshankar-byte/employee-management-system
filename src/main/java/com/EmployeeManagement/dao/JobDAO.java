package com.EmployeeManagement.dao;

import com.EmployeeManagement.entity.Job;
import java.util.List;
import java.util.Optional;

public interface JobDAO {
    Job save(Job job);
    Optional<Job> findById(String id);
    List<Job> findAll();
    void deleteById(String id);
}
