package com.EmployeeManagement.dao;

import com.EmployeeManagement.entity.EmployeeShift;
import com.EmployeeManagement.entity.Shift;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmployeeShiftDAO {
    EmployeeShift save(EmployeeShift employeeShift);
    Optional<EmployeeShift> findById(String id);
    List<EmployeeShift> findAll();
    List<EmployeeShift> findByEmployeeId(String employeeId);
    List<EmployeeShift> findAssignmentsForDate(LocalDate date);
    Optional<EmployeeShift> findByEmployeeIdAndDate(String employeeId, LocalDate date);
    Optional<Shift> findShiftByEmployeeIdAndDate(String employeeId, LocalDate date);
    void deleteById(String id);
    void deleteByEmployeeId(String employeeId);
}
