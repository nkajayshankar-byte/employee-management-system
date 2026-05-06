package com.EmployeeManagement.dao.impl;

import com.EmployeeManagement.dao.EmployeeShiftDAO;
import com.EmployeeManagement.entity.EmployeeShift;
import com.EmployeeManagement.entity.Shift;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class EmployeeShiftDAOImpl implements EmployeeShiftDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public EmployeeShift save(EmployeeShift es) {
        if (es.getId() == null || es.getId().isEmpty()) {
            es.setId(UUID.randomUUID().toString());
            String sql = "INSERT INTO employee_shifts (id, employeeId, shiftId, startDate, endDate) VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, es.getId(), es.getEmployeeId(), es.getShiftId(), es.getStartDate(), es.getEndDate());
        } else {
            String sql = "UPDATE employee_shifts SET employeeId = ?, shiftId = ?, startDate = ?, endDate = ? WHERE id = ?";
            jdbcTemplate.update(sql, es.getEmployeeId(), es.getShiftId(), es.getStartDate(), es.getEndDate(), es.getId());
        }
        return es;
    }

    @Override
    public Optional<EmployeeShift> findById(String id) {
        String sql = "SELECT * FROM employee_shifts WHERE id = ?";
        try {
            EmployeeShift es = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(EmployeeShift.class), id);
            return Optional.ofNullable(es);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<EmployeeShift> findAll() {
        String sql = "SELECT * FROM employee_shifts";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(EmployeeShift.class));
    }

    @Override
    public List<EmployeeShift> findByEmployeeId(String employeeId) {
        String sql = "SELECT * FROM employee_shifts WHERE employeeId = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(EmployeeShift.class), employeeId);
    }

    @Override
    public List<EmployeeShift> findAssignmentsForDate(LocalDate date) {
        String sql = "SELECT * FROM employee_shifts WHERE startDate <= ? AND endDate >= ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(EmployeeShift.class), date, date);
    }

    @Override
    public Optional<EmployeeShift> findByEmployeeIdAndDate(String employeeId, LocalDate date) {
        String sql = "SELECT * FROM employee_shifts WHERE employeeId = ? AND startDate <= ? AND endDate >= ?";
        try {
            EmployeeShift es = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(EmployeeShift.class), employeeId, date, date);
            return Optional.ofNullable(es);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Shift> findShiftByEmployeeIdAndDate(String employeeId, LocalDate date) {
        String sql = "SELECT s.* FROM shifts s JOIN employee_shifts es ON s.id = es.shiftId WHERE es.employeeId = ? AND es.startDate <= ? AND es.endDate >= ?";
        try {
            Shift shift = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Shift.class), employeeId, date, date);
            return Optional.ofNullable(shift);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteById(String id) {
        String sql = "DELETE FROM employee_shifts WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void deleteByEmployeeId(String employeeId) {
        String sql = "DELETE FROM employee_shifts WHERE employeeId = ?";
        jdbcTemplate.update(sql, employeeId);
    }
}
