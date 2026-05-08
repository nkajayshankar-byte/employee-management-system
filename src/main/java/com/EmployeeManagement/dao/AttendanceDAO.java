package com.EmployeeManagement.dao;

import com.EmployeeManagement.entity.Attendance;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceDAO {
    Attendance save(Attendance attendance);
    Optional<Attendance> findById(Long id);
    List<Attendance> findAll();
    List<Attendance> findByEmployeeId(Long employeeId);
    Optional<Attendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date);
    List<Attendance> findByDate(LocalDate date);
    void deleteById(Long id);
    void deleteByEmployeeId(Long employeeId);
}
