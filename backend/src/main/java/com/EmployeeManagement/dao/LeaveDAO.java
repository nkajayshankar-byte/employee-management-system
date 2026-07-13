package com.EmployeeManagement.dao;

import com.EmployeeManagement.entity.Leave;
import java.util.List;
import java.util.Optional;

public interface LeaveDAO {
    Leave save(Leave leave);
    Optional<Leave> findById(Long id);
    List<Leave> findAll();
    List<Leave> findByEmployeeId(Long employeeId);
    List<Leave> findByStatus(String status);
    List<Leave> findByEmployeeIdAndStatus(Long employeeId, String status);
    List<Leave> findPendingLeaves();
    void deleteById(Long id);
}
