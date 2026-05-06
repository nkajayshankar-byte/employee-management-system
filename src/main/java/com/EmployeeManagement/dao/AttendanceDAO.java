package com.EmployeeManagement.dao;

import com.EmployeeManagement.entity.Attendance;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceDAO {
    Attendance save(Attendance attendance);
    Optional<Attendance> findById(String id);
    List<Attendance> findAll();
    List<Attendance> findByEmployeeId(String employeeId);
    Optional<Attendance> findByEmployeeIdAndDate(String employeeId, LocalDate date);
    List<Attendance> findByDate(LocalDate date);
    void deleteById(String id);
    void deleteByEmployeeId(String employeeId);
}
