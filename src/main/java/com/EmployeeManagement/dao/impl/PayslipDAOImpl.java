package com.EmployeeManagement.dao.impl;

import com.EmployeeManagement.dao.PayslipDAO;
import com.EmployeeManagement.entity.Payslip;
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
public class PayslipDAOImpl implements PayslipDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Payslip save(Payslip payslip) {
        if (payslip.getId() == null) {
            payslip.onCreate();
            String sql = "INSERT INTO payslips (employeeId, month, year, totalDays, paidDays, grossPay, totalDeductions, lopAmount, netPay, status, pdfUrl, createdAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, payslip.getEmployeeId());
                ps.setInt(2, payslip.getMonth());
                ps.setInt(3, payslip.getYear());
                ps.setObject(4, payslip.getTotalDays());
                ps.setObject(5, payslip.getPaidDays());
                ps.setObject(6, payslip.getGrossPay());
                ps.setObject(7, payslip.getTotalDeductions());
                ps.setObject(8, payslip.getLopAmount());
                ps.setObject(9, payslip.getNetPay());
                ps.setString(10, payslip.getStatus());
                ps.setString(11, payslip.getPdfUrl());
                ps.setObject(12, payslip.getCreatedAt());
                return ps;
            }, keyHolder);

            if (keyHolder.getKey() != null) {
                payslip.setId(keyHolder.getKey().longValue());
            }
        } else {
            String sql = "UPDATE payslips SET totalDays = ?, paidDays = ?, grossPay = ?, totalDeductions = ?, lopAmount = ?, netPay = ?, status = ?, pdfUrl = ? WHERE id = ?";
            jdbcTemplate.update(sql, 
                payslip.getTotalDays(),
                payslip.getPaidDays(),
                payslip.getGrossPay(),
                payslip.getTotalDeductions(),
                payslip.getLopAmount(),
                payslip.getNetPay(),
                payslip.getStatus(), 
                payslip.getPdfUrl(), 
                payslip.getId()
            );
        }
        return payslip;
    }

    @Override
    public Optional<Payslip> findById(Long id) {
        String sql = "SELECT * FROM payslips WHERE id = ?";
        try {
            Payslip payslip = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Payslip.class), id);
            return Optional.ofNullable(payslip);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Payslip> findByEmployeeId(Long employeeId) {
        String sql = "SELECT * FROM payslips WHERE employeeId = ? ORDER BY year DESC, month DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Payslip.class), employeeId);
    }

    @Override
    public Optional<Payslip> findByEmployeeIdAndMonthAndYear(Long employeeId, int month, int year) {
        String sql = "SELECT * FROM payslips WHERE employeeId = ? AND month = ? AND year = ?";
        try {
            Payslip payslip = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Payslip.class), employeeId, month, year);
            return Optional.ofNullable(payslip);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Payslip> findAll() {
        String sql = "SELECT * FROM payslips ORDER BY year DESC, month DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Payslip.class));
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM payslips WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
