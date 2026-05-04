package com.EmployeeManagement.dao;

import com.EmployeeManagement.entity.Leave;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LeaveDAO {
    Leave save(Leave leave);
    Optional<Leave> findById(String id);
    List<Leave> findAll();
    List<Leave> findByEmployeeId(String employeeId);
    List<Leave> findByStatus(String status);
    List<Leave> findByEmployeeIdAndStatus(String employeeId, String status);
    List<Leave> findLeavesByDateRange(String employeeId, LocalDate startDate, LocalDate endDate);
    List<Leave> findPendingLeaves();
    List<Leave> findApprovedLeavesByTypeAndDate(String leaveType, String employeeId, LocalDate date);
    void deleteById(String id);
}
