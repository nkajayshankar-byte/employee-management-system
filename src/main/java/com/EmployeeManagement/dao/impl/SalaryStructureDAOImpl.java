package com.EmployeeManagement.dao.impl;

import com.EmployeeManagement.dao.SalaryStructureDAO;
import com.EmployeeManagement.entity.SalaryStructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Repository
public class SalaryStructureDAOImpl implements SalaryStructureDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public SalaryStructure save(SalaryStructure salaryStructure) {
        if (salaryStructure.getId() == null) {
            salaryStructure.onCreate();
            String sql = "INSERT INTO salary_structures (employeeId, baseSalary, hra, otherAllowances, taxDeductions, providentFund, netSalary, accountNumber, createdAt, updatedAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, salaryStructure.getEmployeeId());
                ps.setObject(2, salaryStructure.getBaseSalary());
                ps.setObject(3, salaryStructure.getHra());
                ps.setObject(4, salaryStructure.getOtherAllowances());
                ps.setObject(5, salaryStructure.getTaxDeductions());
                ps.setObject(6, salaryStructure.getProvidentFund());
                ps.setObject(7, salaryStructure.getNetSalary());
                ps.setString(8, salaryStructure.getAccountNumber());
                ps.setObject(9, LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                ps.setObject(10, LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                return ps;
            }, keyHolder);

            if (keyHolder.getKey() != null) {
                salaryStructure.setId(keyHolder.getKey().longValue());
            }
        } else {
            salaryStructure.onUpdate();
            String sql = "UPDATE salary_structures SET baseSalary = ?, hra = ?, otherAllowances = ?, taxDeductions = ?, providentFund = ?, netSalary = ?, accountNumber = ?, updatedAt = ? WHERE employeeId = ?";
            jdbcTemplate.update(sql, 
                salaryStructure.getBaseSalary(), 
                salaryStructure.getHra(), 
                salaryStructure.getOtherAllowances(), 
                salaryStructure.getTaxDeductions(), 
                salaryStructure.getProvidentFund(), 
                salaryStructure.getNetSalary(), 
                salaryStructure.getAccountNumber(),
                LocalDateTime.now(ZoneId.of("Asia/Kolkata")),
                salaryStructure.getEmployeeId()
            );
        }
        return salaryStructure;
    }

    @Override
    public Optional<SalaryStructure> findById(Long id) {
        String sql = "SELECT * FROM salary_structures WHERE id = ?";
        try {
            SalaryStructure structure = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(SalaryStructure.class), id);
            return Optional.ofNullable(structure);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<SalaryStructure> findByEmployeeId(Long employeeId) {
        String sql = "SELECT * FROM salary_structures WHERE employeeId = ?";
        try {
            SalaryStructure structure = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(SalaryStructure.class), employeeId);
            return Optional.ofNullable(structure);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<SalaryStructure> findAll() {
        String sql = "SELECT * FROM salary_structures";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(SalaryStructure.class));
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM salary_structures WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
