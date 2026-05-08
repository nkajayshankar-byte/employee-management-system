package com.EmployeeManagement.dao.impl;

import com.EmployeeManagement.dao.EmployeeShiftDAO;
import com.EmployeeManagement.entity.EmployeeShift;
import com.EmployeeManagement.entity.Shift;
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
public class EmployeeShiftDAOImpl implements EmployeeShiftDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public EmployeeShift save(EmployeeShift employeeShift) {
        if (employeeShift.getId() == null) {
            String sql = "INSERT INTO employee_shifts (employeeId, shiftId, startDate, endDate) VALUES (?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, employeeShift.getEmployeeId());
                ps.setLong(2, employeeShift.getShiftId());
                ps.setObject(3, employeeShift.getStartDate());
                ps.setObject(4, employeeShift.getEndDate());
                return ps;
            }, keyHolder);

            employeeShift.setId(keyHolder.getKey().longValue());
        } else {
            String sql = "UPDATE employee_shifts SET employeeId = ?, shiftId = ?, startDate = ?, endDate = ? WHERE id = ?";
            jdbcTemplate.update(sql,
                employeeShift.getEmployeeId(),
                employeeShift.getShiftId(),
                employeeShift.getStartDate(),
                employeeShift.getEndDate(),
                employeeShift.getId()
            );
        }
        return employeeShift;
    }

    @Override
    public Optional<EmployeeShift> findById(Long id) {
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
    public List<EmployeeShift> findByEmployeeId(Long employeeId) {
        String sql = "SELECT * FROM employee_shifts WHERE employeeId = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(EmployeeShift.class), employeeId);
    }

    @Override
    public List<EmployeeShift> findAssignmentsForDate(LocalDate date) {
        String sql = "SELECT * FROM employee_shifts WHERE ? BETWEEN startDate AND endDate";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(EmployeeShift.class), date);
    }

    @Override
    public Optional<EmployeeShift> findByEmployeeIdAndDate(Long employeeId, LocalDate date) {
        String sql = "SELECT * FROM employee_shifts WHERE employeeId = ? AND ? BETWEEN startDate AND endDate";
        try {
            EmployeeShift es = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(EmployeeShift.class), employeeId, date);
            return Optional.ofNullable(es);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Shift> findShiftByEmployeeIdAndDate(Long employeeId, LocalDate date) {
        String sql = "SELECT s.* FROM shifts s " +
                     "JOIN employee_shifts es ON s.id = es.shiftId " +
                     "WHERE es.employeeId = ? AND ? BETWEEN es.startDate AND es.endDate";
        try {
            Shift shift = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Shift.class), employeeId, date);
            return Optional.ofNullable(shift);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM employee_shifts WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void deleteByEmployeeId(Long employeeId) {
        String sql = "DELETE FROM employee_shifts WHERE employeeId = ?";
        jdbcTemplate.update(sql, employeeId);
    }
}
