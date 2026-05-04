package com.EmployeeManagement.dao.impl;

import com.EmployeeManagement.dao.UserDAO;
import com.EmployeeManagement.entity.Role;
import com.EmployeeManagement.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserDAOImpl implements UserDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public User save(User user) {
        if (user.getId() == null || user.getId().isEmpty()) {
            user.setId(UUID.randomUUID().toString());
            String sql = "INSERT INTO users (id, email, password, name, mobile, address, role, imageUrl, skills, jobRole, companyInfo, createdAt, updatedAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, 
                user.getId(), 
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
                user.getCreatedAt(), 
                user.getUpdatedAt()
            );
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
    public Optional<User> findById(String id) {
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
    public void deleteById(String id) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
