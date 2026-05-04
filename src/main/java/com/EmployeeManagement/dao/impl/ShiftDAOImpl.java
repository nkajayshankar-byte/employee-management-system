package com.EmployeeManagement.dao.impl;

import com.EmployeeManagement.dao.ShiftDAO;
import com.EmployeeManagement.entity.Shift;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ShiftDAOImpl implements ShiftDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Shift> shiftRowMapper = new RowMapper<Shift>() {
        @Override
        public Shift mapRow(ResultSet rs, int rowNum) throws SQLException {
            Shift shift = new Shift();
            shift.setId(rs.getString("id"));
            shift.setShiftName(rs.getString("shiftName"));
            Time startTime = rs.getTime("startTime");
            if (startTime != null) shift.setStartTime(startTime.toLocalTime());
            Time endTime = rs.getTime("endTime");
            if (endTime != null) shift.setEndTime(endTime.toLocalTime());
            shift.setDescription(rs.getString("description"));
            return shift;
        }
    };

    @Override
    public Shift save(Shift shift) {
        if (shift.getId() == null || shift.getId().isEmpty()) {
            shift.setId(UUID.randomUUID().toString());
            String sql = "INSERT INTO shifts (id, shiftName, startTime, endTime, description) VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, shift.getId(), shift.getShiftName(), shift.getStartTime(), shift.getEndTime(), shift.getDescription());
        } else {
            String sql = "UPDATE shifts SET shiftName = ?, startTime = ?, endTime = ?, description = ? WHERE id = ?";
            jdbcTemplate.update(sql, shift.getShiftName(), shift.getStartTime(), shift.getEndTime(), shift.getDescription(), shift.getId());
        }
        return shift;
    }

    @Override
    public Optional<Shift> findById(String id) {
        String sql = "SELECT * FROM shifts WHERE id = ?";
        try {
            Shift shift = jdbcTemplate.queryForObject(sql, shiftRowMapper, id);
            return Optional.ofNullable(shift);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Shift> findAll() {
        String sql = "SELECT * FROM shifts";
        return jdbcTemplate.query(sql, shiftRowMapper);
    }

    @Override
    public void deleteById(String id) {
        String sql = "DELETE FROM shifts WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
