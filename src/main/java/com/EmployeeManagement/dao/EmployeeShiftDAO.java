package com.EmployeeManagement.dao;

import com.EmployeeManagement.entity.EmployeeShift;
import com.EmployeeManagement.entity.Shift;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmployeeShiftDAO {
    EmployeeShift save(EmployeeShift employeeShift);
    Optional<EmployeeShift> findById(Long id);
    List<EmployeeShift> findAll();
    List<EmployeeShift> findByEmployeeId(Long employeeId);
    List<EmployeeShift> findAssignmentsForDate(LocalDate date);
    Optional<EmployeeShift> findByEmployeeIdAndDate(Long employeeId, LocalDate date);
    Optional<Shift> findShiftByEmployeeIdAndDate(Long employeeId, LocalDate date);
    void deleteById(Long id);
    void deleteByEmployeeId(Long employeeId);
}
