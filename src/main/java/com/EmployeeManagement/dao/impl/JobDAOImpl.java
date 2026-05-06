package com.EmployeeManagement.dao.impl;

import com.EmployeeManagement.dao.JobDAO;
import com.EmployeeManagement.entity.Job;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JobDAOImpl implements JobDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private final RowMapper<Job> jobRowMapper = new RowMapper<Job>() {
        @Override
        public Job mapRow(ResultSet rs, int rowNum) throws SQLException {
            Job job = new Job();
            job.setId(rs.getString("id"));
            job.setTitle(rs.getString("title"));
            job.setDepartment(rs.getString("department"));
            job.setLocation(rs.getString("location"));
            job.setType(rs.getString("type"));
            job.setDescription(rs.getString("description"));
            job.setKeyResponsibilities(rs.getString("keyResponsibilities"));
            job.setMinSalary(rs.getInt("minSalary"));
            job.setMaxSalary(rs.getInt("maxSalary"));
            try {
                String skillsJson = rs.getString("requiredSkills");
                if (skillsJson != null) {
                    job.setRequiredSkills(objectMapper.readValue(skillsJson, new TypeReference<List<String>>() {}));
                }
            } catch (Exception e) {
                job.setRequiredSkills(new ArrayList<>());
            }
            job.setCreatedAt(rs.getObject("createdAt", LocalDateTime.class));
            job.setIsActive(rs.getBoolean("isActive"));
            return job;
        }
    };

    @Override
    public Job save(Job job) {
        if (job.getId() == null || job.getId().isEmpty()) {
            job.setId(UUID.randomUUID().toString());
            String sql = "INSERT INTO jobs (id, title, department, location, type, description, keyResponsibilities, minSalary, maxSalary, requiredSkills, createdAt, isActive) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try {
                jdbcTemplate.update(sql,
                    job.getId(),
                    job.getTitle(),
                    job.getDepartment(),
                    job.getLocation(),
                    job.getType(),
                    job.getDescription(),
                    job.getKeyResponsibilities(),
                    job.getMinSalary(),
                    job.getMaxSalary(),
                    objectMapper.writeValueAsString(job.getRequiredSkills()),
                    job.getCreatedAt(),
                    job.getIsActive()
                );
            } catch (Exception e) {
                throw new RuntimeException("Error saving job", e);
            }
        } else {
            String sql = "UPDATE jobs SET title = ?, department = ?, location = ?, type = ?, description = ?, keyResponsibilities = ?, minSalary = ?, maxSalary = ?, requiredSkills = ?, isActive = ? WHERE id = ?";
            try {
                jdbcTemplate.update(sql,
                    job.getTitle(),
                    job.getDepartment(),
                    job.getLocation(),
                    job.getType(),
                    job.getDescription(),
                    job.getKeyResponsibilities(),
                    job.getMinSalary(),
                    job.getMaxSalary(),
                    objectMapper.writeValueAsString(job.getRequiredSkills()),
                    job.getIsActive(),
                    job.getId()
                );
            } catch (Exception e) {
                throw new RuntimeException("Error updating job", e);
            }
        }
        return job;
    }

    @Override
    public Optional<Job> findById(String id) {
        String sql = "SELECT * FROM jobs WHERE id = ?";
        try {
            Job job = jdbcTemplate.queryForObject(sql, jobRowMapper, id);
            return Optional.ofNullable(job);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Job> findAll() {
        String sql = "SELECT * FROM jobs";
        return jdbcTemplate.query(sql, jobRowMapper);
    }

    @Override
    public void deleteById(String id) {
        String sql = "DELETE FROM jobs WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public String findHiredJobTitleByEmployeeId(String employeeId) {
        String sql = "SELECT j.title FROM jobs j JOIN job_applications ja ON j.id = ja.jobId WHERE ja.employeeId = ? AND ja.status = 'HIRED' LIMIT 1";
        try {
            return jdbcTemplate.queryForObject(sql, String.class, employeeId);
        } catch (Exception e) {
            return null;
        }
    }
}
