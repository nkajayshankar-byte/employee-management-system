package com.EmployeeManagement.dao.impl;

import com.EmployeeManagement.dao.AttendanceDAO;
import com.EmployeeManagement.entity.Attendance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AttendanceDAOImpl implements AttendanceDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Attendance save(Attendance attendance) {
        if (attendance.getId() == null || attendance.getId().isEmpty()) {
            attendance.setId(UUID.randomUUID().toString());
            String sql = "INSERT INTO attendance (id, employeeId, date, checkInTime, checkOutTime, status, workingHours) VALUES (?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql,
                attendance.getId(),
                attendance.getEmployeeId(),
                attendance.getDate(),
                attendance.getCheckInTime(),
                attendance.getCheckOutTime(),
                attendance.getStatus(),
                attendance.getWorkingHours()
            );
        } else {
            String sql = "UPDATE attendance SET employeeId = ?, date = ?, checkInTime = ?, checkOutTime = ?, status = ?, workingHours = ? WHERE id = ?";
            jdbcTemplate.update(sql,
                attendance.getEmployeeId(),
                attendance.getDate(),
                attendance.getCheckInTime(),
                attendance.getCheckOutTime(),
                attendance.getStatus(),
                attendance.getWorkingHours(),
                attendance.getId()
            );
        }
        return attendance;
    }

    @Override
    public Optional<Attendance> findById(String id) {
        String sql = "SELECT * FROM attendance WHERE id = ?";
        try {
            Attendance attendance = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Attendance.class), id);
            return Optional.ofNullable(attendance);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Attendance> findAll() {
        String sql = "SELECT * FROM attendance";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Attendance.class));
    }

    @Override
    public List<Attendance> findByEmployeeId(String employeeId) {
        String sql = "SELECT * FROM attendance WHERE employeeId = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Attendance.class), employeeId);
    }

    @Override
    public Optional<Attendance> findByEmployeeIdAndDate(String employeeId, LocalDate date) {
        String sql = "SELECT * FROM attendance WHERE employeeId = ? AND date = ?";
        try {
            Attendance attendance = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Attendance.class), employeeId, date);
            return Optional.ofNullable(attendance);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Attendance> findByDate(LocalDate date) {
        String sql = "SELECT * FROM attendance WHERE date = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Attendance.class), date);
    }

    @Override
    public void deleteById(String id) {
        String sql = "DELETE FROM attendance WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
