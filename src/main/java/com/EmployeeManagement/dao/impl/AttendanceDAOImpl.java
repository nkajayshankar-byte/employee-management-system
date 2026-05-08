package com.EmployeeManagement.dao.impl;

import com.EmployeeManagement.dao.AttendanceDAO;
import com.EmployeeManagement.entity.Attendance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class AttendanceDAOImpl implements AttendanceDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Attendance save(Attendance attendance) {
        if (attendance.getId() == null) {
            String sql = "INSERT INTO attendance (employeeId, date, checkInTime, checkOutTime, status, workingHours) VALUES (?, ?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, attendance.getEmployeeId());
                ps.setObject(2, attendance.getDate());
                ps.setObject(3, attendance.getCheckInTime());
                ps.setObject(4, attendance.getCheckOutTime());
                ps.setString(5, attendance.getStatus());
                ps.setObject(6, attendance.getWorkingHours());
                return ps;
            }, keyHolder);

            attendance.setId(keyHolder.getKey().longValue());
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
    public Optional<Attendance> findById(Long id) {
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
    public List<Attendance> findByEmployeeId(Long employeeId) {
        String sql = "SELECT * FROM attendance WHERE employeeId = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Attendance.class), employeeId);
    }

    @Override
    public Optional<Attendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date) {
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
    public void deleteById(Long id) {
        String sql = "DELETE FROM attendance WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void deleteByEmployeeId(Long employeeId) {
        String sql = "DELETE FROM attendance WHERE employeeId = ?";
        jdbcTemplate.update(sql, employeeId);
    }
}
