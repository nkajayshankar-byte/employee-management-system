package com.EmployeeManagement.dao.impl;

import com.EmployeeManagement.dao.ShiftDAO;
import com.EmployeeManagement.entity.Shift;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Time;
import java.util.List;
import java.util.Optional;

@Repository
public class ShiftDAOImpl implements ShiftDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Shift save(Shift shift) {
        if (shift.getId() == null) {
            String sql = "INSERT INTO shifts (shiftName, startTime, endTime, description) VALUES (?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, shift.getShiftName());
                ps.setTime(2, shift.getStartTime() != null ? Time.valueOf(shift.getStartTime()) : null);
                ps.setTime(3, shift.getEndTime() != null ? Time.valueOf(shift.getEndTime()) : null);
                ps.setString(4, shift.getDescription());
                return ps;
            }, keyHolder);

            shift.setId(keyHolder.getKey().longValue());
        } else {
            String sql = "UPDATE shifts SET shiftName = ?, startTime = ?, endTime = ?, description = ? WHERE id = ?";
            jdbcTemplate.update(sql,
                shift.getShiftName(),
                shift.getStartTime() != null ? Time.valueOf(shift.getStartTime()) : null,
                shift.getEndTime() != null ? Time.valueOf(shift.getEndTime()) : null,
                shift.getDescription(),
                shift.getId()
            );
        }
        return shift;
    }

    @Override
    public Optional<Shift> findById(Long id) {
        String sql = "SELECT * FROM shifts WHERE id = ?";
        try {
            Shift shift = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Shift.class), id);
            return Optional.ofNullable(shift);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Shift> findAll() {
        String sql = "SELECT * FROM shifts";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Shift.class));
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM shifts WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
