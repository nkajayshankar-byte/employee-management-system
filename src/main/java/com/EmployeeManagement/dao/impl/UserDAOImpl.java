package com.EmployeeManagement.dao.impl;

import com.EmployeeManagement.dao.UserDAO;
import com.EmployeeManagement.entity.Role;
import com.EmployeeManagement.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class UserDAOImpl implements UserDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            String sql = "INSERT INTO users (email, password, name, mobile, address, role, imageUrl, skills, jobRole, companyInfo, createdAt, updatedAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, user.getEmail());
                ps.setString(2, user.getPassword());
                ps.setString(3, user.getName());
                ps.setString(4, user.getMobile());
                ps.setString(5, user.getAddress());
                ps.setString(6, user.getRole() != null ? user.getRole().name() : Role.USER.name());
                ps.setString(7, user.getImageUrl());
                ps.setString(8, user.getSkills());
                ps.setString(9, user.getJobRole());
                ps.setString(10, user.getCompanyInfo());
                ps.setObject(11, user.getCreatedAt());
                ps.setObject(12, user.getUpdatedAt());
                return ps;
            }, keyHolder);

            user.setId(keyHolder.getKey().longValue());
        } else {
            String sql = "UPDATE users SET email = ?, password = ?, name = ?, mobile = ?, address = ?, role = ?, imageUrl = ?, skills = ?, jobRole = ?, companyInfo = ?, updatedAt = ? WHERE id = ?";
            jdbcTemplate.update(sql, 
                user.getEmail(), 
                user.getPassword(), 
                user.getName(), 
                user.getMobile(), 
                user.getAddress(), 
                user.getRole() != null ? user.getRole().name() : null, 
                user.getImageUrl(), 
                user.getSkills(), 
                user.getJobRole(), 
                user.getCompanyInfo(), 
                user.getUpdatedAt(), 
                user.getId()
            );
        }
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(User.class), id);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(User.class), email);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class));
    }

    @Override
    public long countByRole(Role role) {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, role.name());
        return count != null ? count : 0;
    }

    @Override
    public List<User> findByRole(Role role) {
        String sql = "SELECT * FROM users WHERE role = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class), role.name());
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
