package com.EmployeeManagement.dao.impl;

import com.EmployeeManagement.dao.LeaveDAO;
import com.EmployeeManagement.entity.Leave;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class LeaveDAOImpl implements LeaveDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Leave> leaveRowMapper = new RowMapper<Leave>() {
        @Override
        public Leave mapRow(ResultSet rs, int rowNum) throws SQLException {
            Leave leave = new Leave();
            leave.setId(rs.getString("id"));
            leave.setEmployeeId(rs.getString("employeeId"));
            leave.setEmployeeName(rs.getString("employeeName"));
            leave.setLeaveType(rs.getString("leaveType"));
            leave.setStartDate(rs.getObject("startDate", LocalDate.class));
            leave.setEndDate(rs.getObject("endDate", LocalDate.class));
            leave.setReason(rs.getString("reason"));
            leave.setStatus(rs.getString("status"));
            leave.setApproverId(rs.getString("approverId"));
            leave.setApproverName(rs.getString("approverName"));
            leave.setApprovedAt(rs.getObject("approvedAt", LocalDateTime.class));
            leave.setApproverComments(rs.getString("approverComments"));
            leave.setCreatedAt(rs.getObject("createdAt", LocalDateTime.class));
            leave.setUpdatedAt(rs.getObject("updatedAt", LocalDateTime.class));
            leave.setNumberOfDays(rs.getObject("numberOfDays", Integer.class));
            return leave;
        }
    };

    @Override
    public Leave save(Leave leave) {
        if (leave.getId() == null || leave.getId().isEmpty()) {
            leave.setId(UUID.randomUUID().toString());
            String sql = "INSERT INTO leaves (id, employeeId, employeeName, leaveType, startDate, endDate, reason, status, approverId, approverName, approvedAt, approverComments, createdAt, updatedAt, numberOfDays) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql,
                leave.getId(),
                leave.getEmployeeId(),
                leave.getEmployeeName(),
                leave.getLeaveType(),
                leave.getStartDate(),
                leave.getEndDate(),
                leave.getReason(),
                leave.getStatus(),
                leave.getApproverId(),
                leave.getApproverName(),
                leave.getApprovedAt(),
                leave.getApproverComments(),
                leave.getCreatedAt(),
                leave.getUpdatedAt(),
                leave.getNumberOfDays()
            );
        } else {
            String sql = "UPDATE leaves SET employeeId = ?, employeeName = ?, leaveType = ?, startDate = ?, endDate = ?, reason = ?, status = ?, approverId = ?, approverName = ?, approvedAt = ?, approverComments = ?, updatedAt = ?, numberOfDays = ? WHERE id = ?";
            jdbcTemplate.update(sql,
                leave.getEmployeeId(),
                leave.getEmployeeName(),
                leave.getLeaveType(),
                leave.getStartDate(),
                leave.getEndDate(),
                leave.getReason(),
                leave.getStatus(),
                leave.getApproverId(),
                leave.getApproverName(),
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
    public Optional<Leave> findById(String id) {
        String sql = "SELECT * FROM leaves WHERE id = ?";
        try {
            Leave leave = jdbcTemplate.queryForObject(sql, leaveRowMapper, id);
            return Optional.ofNullable(leave);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Leave> findAll() {
        String sql = "SELECT * FROM leaves";
        return jdbcTemplate.query(sql, leaveRowMapper);
    }

    @Override
    public List<Leave> findByEmployeeId(String employeeId) {
        String sql = "SELECT * FROM leaves WHERE employeeId = ?";
        return jdbcTemplate.query(sql, leaveRowMapper, employeeId);
    }

    @Override
    public List<Leave> findByStatus(String status) {
        String sql = "SELECT * FROM leaves WHERE status = ?";
        return jdbcTemplate.query(sql, leaveRowMapper, status);
    }

    @Override
    public List<Leave> findByEmployeeIdAndStatus(String employeeId, String status) {
        String sql = "SELECT * FROM leaves WHERE employeeId = ? AND status = ?";
        return jdbcTemplate.query(sql, leaveRowMapper, employeeId, status);
    }

    @Override
    public List<Leave> findLeavesByDateRange(String employeeId, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT * FROM leaves WHERE employeeId = ? AND startDate >= ? AND startDate <= ?";
        return jdbcTemplate.query(sql, leaveRowMapper, employeeId, startDate, endDate);
    }

    @Override
    public List<Leave> findPendingLeaves() {
        String sql = "SELECT * FROM leaves WHERE status = 'PENDING'";
        return jdbcTemplate.query(sql, leaveRowMapper);
    }

    @Override
    public List<Leave> findApprovedLeavesByTypeAndDate(String leaveType, String employeeId, LocalDate date) {
        String sql = "SELECT * FROM leaves WHERE leaveType = ? AND status = 'APPROVED' AND employeeId = ? AND startDate >= ?";
        return jdbcTemplate.query(sql, leaveRowMapper, leaveType, employeeId, date);
    }

    @Override
    public void deleteById(String id) {
        String sql = "DELETE FROM leaves WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
