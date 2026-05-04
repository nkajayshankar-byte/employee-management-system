package com.EmployeeManagement.dao.impl;

import com.EmployeeManagement.dao.ApplicationDAO;
import com.EmployeeManagement.entity.JobApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ApplicationDAOImpl implements ApplicationDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public JobApplication save(JobApplication app) {
        if (app.getId() == null || app.getId().isEmpty()) {
            app.setId(UUID.randomUUID().toString());
            String sql = "INSERT INTO job_applications (id, jobId, employeeId, employeeName, employeeEmail, resumeUrl, status, appliedDate) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql,
                app.getId(),
                app.getJobId(),
                app.getEmployeeId(),
                app.getEmployeeName(),
                app.getEmployeeEmail(),
                app.getResumeUrl(),
                app.getStatus(),
                app.getAppliedDate()
            );
        } else {
            String sql = "UPDATE job_applications SET jobId = ?, employeeId = ?, employeeName = ?, employeeEmail = ?, resumeUrl = ?, status = ?, appliedDate = ? WHERE id = ?";
            jdbcTemplate.update(sql,
                app.getJobId(),
                app.getEmployeeId(),
                app.getEmployeeName(),
                app.getEmployeeEmail(),
                app.getResumeUrl(),
                app.getStatus(),
                app.getAppliedDate(),
                app.getId()
            );
        }
        return app;
    }

    @Override
    public Optional<JobApplication> findById(String id) {
        String sql = "SELECT * FROM job_applications WHERE id = ?";
        try {
            JobApplication app = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(JobApplication.class), id);
            return Optional.ofNullable(app);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<JobApplication> findAll() {
        String sql = "SELECT * FROM job_applications";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(JobApplication.class));
    }

    @Override
    public List<JobApplication> findByJobId(String jobId) {
        String sql = "SELECT * FROM job_applications WHERE jobId = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(JobApplication.class), jobId);
    }

    @Override
    public List<JobApplication> findByEmployeeId(String employeeId) {
        String sql = "SELECT * FROM job_applications WHERE employeeId = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(JobApplication.class), employeeId);
    }

    @Override
    public boolean existsByJobIdAndEmployeeId(String jobId, String employeeId) {
        String sql = "SELECT COUNT(*) FROM job_applications WHERE jobId = ? AND employeeId = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, jobId, employeeId);
        return count != null && count > 0;
    }

    @Override
    public void deleteById(String id) {
        String sql = "DELETE FROM job_applications WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void deleteByJobId(String jobId) {
        String sql = "DELETE FROM job_applications WHERE jobId = ?";
        jdbcTemplate.update(sql, jobId);
    }
}
