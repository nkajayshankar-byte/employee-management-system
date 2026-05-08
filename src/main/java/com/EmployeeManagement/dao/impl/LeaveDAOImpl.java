package com.EmployeeManagement.dao.impl;

import com.EmployeeManagement.dao.LeaveDAO;
import com.EmployeeManagement.entity.Leave;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class LeaveDAOImpl implements LeaveDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Leave> rowMapper = (rs, rowNum) -> {
        Leave leave = new Leave();
        leave.setId(rs.getLong("id"));
        leave.setEmployeeId(rs.getLong("employeeId"));
        leave.setLeaveType(rs.getString("leaveType"));
        leave.setStartDate(rs.getObject("startDate", LocalDate.class));
        leave.setEndDate(rs.getObject("endDate", LocalDate.class));
        leave.setReason(rs.getString("reason"));
        leave.setStatus(rs.getString("status"));
        leave.setApproverId(rs.getObject("approverId", Long.class));
        leave.setApprovedAt(rs.getTimestamp("approvedAt") != null ? rs.getTimestamp("approvedAt").toLocalDateTime() : null);
        leave.setApproverComments(rs.getString("approverComments"));
        leave.setCreatedAt(rs.getTimestamp("createdAt") != null ? rs.getTimestamp("createdAt").toLocalDateTime() : null);
        leave.setUpdatedAt(rs.getTimestamp("updatedAt") != null ? rs.getTimestamp("updatedAt").toLocalDateTime() : null);
        leave.setNumberOfDays(rs.getInt("numberOfDays"));
        
        // Joined fields
        leave.setEmployeeName(rs.getString("employeeName"));
        leave.setApproverName(rs.getString("approverName"));
        
        return leave;
    };

    private final String BASE_SELECT = "SELECT l.*, u.name AS employeeName, a.name AS approverName " +
                                       "FROM leaves l " +
                                       "JOIN users u ON l.employeeId = u.id " +
                                       "LEFT JOIN users a ON l.approverId = a.id ";

    @Override
    public Leave save(Leave leave) {
        if (leave.getId() == null) {
            String sql = "INSERT INTO leaves (employeeId, leaveType, startDate, endDate, reason, status, approverId, approvedAt, approverComments, createdAt, updatedAt, numberOfDays) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, leave.getEmployeeId());
                ps.setString(2, leave.getLeaveType());
                ps.setObject(3, leave.getStartDate());
                ps.setObject(4, leave.getEndDate());
                ps.setString(5, leave.getReason());
                ps.setString(6, leave.getStatus());
                ps.setObject(7, leave.getApproverId());
                ps.setObject(8, leave.getApprovedAt());
                ps.setString(9, leave.getApproverComments());
                ps.setObject(10, leave.getCreatedAt());
                ps.setObject(11, leave.getUpdatedAt());
                ps.setInt(12, leave.getNumberOfDays());
                return ps;
            }, keyHolder);

            leave.setId(keyHolder.getKey().longValue());
        } else {
            String sql = "UPDATE leaves SET employeeId = ?, leaveType = ?, startDate = ?, endDate = ?, reason = ?, status = ?, approverId = ?, approvedAt = ?, approverComments = ?, updatedAt = ?, numberOfDays = ? WHERE id = ?";
            jdbcTemplate.update(sql,
                leave.getEmployeeId(),
                leave.getLeaveType(),
                leave.getStartDate(),
                leave.getEndDate(),
                leave.getReason(),
                leave.getStatus(),
                leave.getApproverId(),
                leave.getApprovedAt(),
                leave.getApproverComments(),
                leave.getUpdatedAt(),
                leave.getNumberOfDays(),
                leave.getId()
            );
        }
        return leave;
    }

    @Override
    public Optional<Leave> findById(Long id) {
        String sql = BASE_SELECT + "WHERE l.id = ?";
        try {
            Leave leave = jdbcTemplate.queryForObject(sql, rowMapper, id);
            return Optional.ofNullable(leave);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Leave> findAll() {
        String sql = BASE_SELECT;
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public List<Leave> findByEmployeeId(Long employeeId) {
        String sql = BASE_SELECT + "WHERE l.employeeId = ?";
        return jdbcTemplate.query(sql, rowMapper, employeeId);
    }

    @Override
    public List<Leave> findByStatus(String status) {
        String sql = BASE_SELECT + "WHERE l.status = ?";
        return jdbcTemplate.query(sql, rowMapper, status);
    }

    @Override
    public List<Leave> findByEmployeeIdAndStatus(Long employeeId, String status) {
        String sql = BASE_SELECT + "WHERE l.employeeId = ? AND l.status = ?";
        return jdbcTemplate.query(sql, rowMapper, employeeId, status);
    }

    @Override
    public List<Leave> findLeavesByDateRange(Long employeeId, LocalDate startDate, LocalDate endDate) {
        String sql = BASE_SELECT + "WHERE l.employeeId = ? AND ((l.startDate BETWEEN ? AND ?) OR (l.endDate BETWEEN ? AND ?))";
        return jdbcTemplate.query(sql, rowMapper, employeeId, startDate, endDate, startDate, endDate);
    }

    @Override
    public List<Leave> findPendingLeaves() {
        String sql = BASE_SELECT + "WHERE l.status = 'PENDING'";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public List<Leave> findApprovedLeavesByTypeAndDate(String leaveType, Long employeeId, LocalDate date) {
        String sql = BASE_SELECT + "WHERE l.leaveType = ? AND l.employeeId = ? AND l.status = 'APPROVED' AND ? BETWEEN l.startDate AND l.endDate";
        return jdbcTemplate.query(sql, rowMapper, leaveType, employeeId, date);
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM leaves WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void deleteByEmployeeId(Long employeeId) {
        String sql = "DELETE FROM leaves WHERE employeeId = ?";
        jdbcTemplate.update(sql, employeeId);
    }
}
