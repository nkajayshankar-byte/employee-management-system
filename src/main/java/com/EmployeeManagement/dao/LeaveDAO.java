package com.EmployeeManagement.dao;

import com.EmployeeManagement.entity.Leave;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LeaveDAO {
    Leave save(Leave leave);
    Optional<Leave> findById(Long id);
    List<Leave> findAll();
    List<Leave> findByEmployeeId(Long employeeId);
    List<Leave> findByStatus(String status);
    List<Leave> findByEmployeeIdAndStatus(Long employeeId, String status);
    List<Leave> findLeavesByDateRange(Long employeeId, LocalDate startDate, LocalDate endDate);
    List<Leave> findPendingLeaves();
    List<Leave> findApprovedLeavesByTypeAndDate(String leaveType, Long employeeId, LocalDate date);
    void deleteById(Long id);
    void deleteByEmployeeId(Long employeeId);
}
