package com.EmployeeManagement.dao.impl;

import com.EmployeeManagement.dao.JobDAO;
import com.EmployeeManagement.entity.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@Repository
public class JobDAOImpl implements JobDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Job> rowMapper = (rs, rowNum) -> {
        Job job = new Job();
        job.setId(rs.getLong("id"));
        job.setTitle(rs.getString("title"));
        job.setDepartment(rs.getString("department"));
        job.setLocation(rs.getString("location"));
        job.setType(rs.getString("type"));
        job.setDescription(rs.getString("description"));
        job.setKeyResponsibilities(rs.getString("keyResponsibilities"));
        job.setMinSalary(rs.getInt("minSalary"));
        job.setMaxSalary(rs.getInt("maxSalary"));
        String skills = rs.getString("requiredSkills");
        if (skills != null && !skills.isEmpty()) {
            job.setRequiredSkills(Arrays.asList(skills.split(",")));
        }
        job.setCreatedAt(rs.getTimestamp("createdAt") != null ? rs.getTimestamp("createdAt").toLocalDateTime() : null);
        job.setIsActive(rs.getBoolean("isActive"));
        return job;
    };

    @Override
    public Job save(Job job) {
        String skills = job.getRequiredSkills() != null ? String.join(",", job.getRequiredSkills()) : "";
        if (job.getId() == null) {
            String sql = "INSERT INTO jobs (title, department, location, type, description, keyResponsibilities, minSalary, maxSalary, requiredSkills, createdAt, isActive) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, job.getTitle());
                ps.setString(2, job.getDepartment());
                ps.setString(3, job.getLocation());
                ps.setString(4, job.getType());
                ps.setString(5, job.getDescription());
                ps.setString(6, job.getKeyResponsibilities());
                ps.setInt(7, job.getMinSalary());
                ps.setInt(8, job.getMaxSalary());
                ps.setString(9, skills);
                ps.setObject(10, job.getCreatedAt());
                ps.setBoolean(11, job.getIsActive() != null ? job.getIsActive() : true);
                return ps;
            }, keyHolder);

            job.setId(keyHolder.getKey().longValue());
        } else {
            String sql = "UPDATE jobs SET title = ?, department = ?, location = ?, type = ?, description = ?, keyResponsibilities = ?, minSalary = ?, maxSalary = ?, requiredSkills = ?, isActive = ? WHERE id = ?";
            jdbcTemplate.update(sql,
                job.getTitle(),
                job.getDepartment(),
                job.getLocation(),
                job.getType(),
                job.getDescription(),
                job.getKeyResponsibilities(),
                job.getMinSalary(),
                job.getMaxSalary(),
                skills,
                job.getIsActive(),
                job.getId()
            );
        }
        return job;
    }

    @Override
    public Optional<Job> findById(Long id) {
        String sql = "SELECT * FROM jobs WHERE id = ?";
        try {
            Job job = jdbcTemplate.queryForObject(sql, rowMapper, id);
            return Optional.ofNullable(job);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Job> findAll() {
        String sql = "SELECT * FROM jobs";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM jobs WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public String findHiredJobTitleByEmployeeId(Long employeeId) {
        String sql = "SELECT j.title FROM jobs j " +
                     "JOIN job_applications ja ON j.id = ja.jobId " +
                     "WHERE ja.employeeId = ? AND ja.status = 'HIRED' LIMIT 1";
        try {
            return jdbcTemplate.queryForObject(sql, String.class, employeeId);
        } catch (Exception e) {
            return null;
        }
    }
}
