package com.EmployeeManagement.dao.impl;

import com.EmployeeManagement.dao.ApplicationDAO;
import com.EmployeeManagement.entity.JobApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class ApplicationDAOImpl implements ApplicationDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<JobApplication> rowMapper = (rs, rowNum) -> {
        JobApplication app = new JobApplication();
        app.setId(rs.getLong("id"));
        app.setJobId(rs.getLong("jobId"));
        app.setEmployeeId(rs.getLong("employeeId"));
        app.setResumeUrl(rs.getString("resumeUrl"));
        app.setStatus(rs.getString("status"));
        app.setAppliedDate(rs.getTimestamp("appliedDate") != null ? rs.getTimestamp("appliedDate").toLocalDateTime() : null);
        
        // Joined fields
        app.setEmployeeName(rs.getString("employeeName"));
        app.setEmployeeEmail(rs.getString("employeeEmail"));
        app.setJobTitle(rs.getString("jobTitle"));
        app.setJobActive(rs.getBoolean("jobActive"));
        
        return app;
    };

    private final String BASE_SELECT = "SELECT ja.*, u.name AS employeeName, u.email AS employeeEmail, j.title AS jobTitle, j.isActive AS jobActive " +
                                       "FROM job_applications ja " +
                                       "JOIN users u ON ja.employeeId = u.id " +
                                       "JOIN jobs j ON ja.jobId = j.id ";

    @Override
    public JobApplication save(JobApplication application) {
        if (application.getId() == null) {
            String sql = "INSERT INTO job_applications (jobId, employeeId, resumeUrl, status, appliedDate) VALUES (?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, application.getJobId());
                ps.setLong(2, application.getEmployeeId());
                ps.setString(3, application.getResumeUrl());
                ps.setString(4, application.getStatus());
                ps.setObject(5, application.getAppliedDate());
                return ps;
            }, keyHolder);

            application.setId(keyHolder.getKey().longValue());
        } else {
            String sql = "UPDATE job_applications SET jobId = ?, employeeId = ?, resumeUrl = ?, status = ?, appliedDate = ? WHERE id = ?";
            jdbcTemplate.update(sql,
                application.getJobId(),
                application.getEmployeeId(),
                application.getResumeUrl(),
                application.getStatus(),
                application.getAppliedDate(),
                application.getId()
            );
        }
        return application;
    }

    @Override
    public Optional<JobApplication> findById(Long id) {
        String sql = BASE_SELECT + "WHERE ja.id = ?";
        try {
            JobApplication app = jdbcTemplate.queryForObject(sql, rowMapper, id);
            return Optional.ofNullable(app);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<JobApplication> findAll() {
        String sql = BASE_SELECT;
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public List<JobApplication> findByJobId(Long jobId) {
        String sql = BASE_SELECT + "WHERE ja.jobId = ?";
        return jdbcTemplate.query(sql, rowMapper, jobId);
    }

    @Override
    public List<JobApplication> findByEmployeeId(Long employeeId) {
        String sql = BASE_SELECT + "WHERE ja.employeeId = ?";
        return jdbcTemplate.query(sql, rowMapper, employeeId);
    }

    @Override
    public boolean existsByJobIdAndEmployeeId(Long jobId, Long employeeId) {
        String sql = "SELECT COUNT(*) FROM job_applications WHERE jobId = ? AND employeeId = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, jobId, employeeId);
        return count != null && count > 0;
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM job_applications WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void deleteByJobId(Long jobId) {
        String sql = "DELETE FROM job_applications WHERE jobId = ?";
        jdbcTemplate.update(sql, jobId);
    }

    @Override
    public void deleteByEmployeeId(Long employeeId) {
        String sql = "DELETE FROM job_applications WHERE employeeId = ?";
        jdbcTemplate.update(sql, employeeId);
    }
}
