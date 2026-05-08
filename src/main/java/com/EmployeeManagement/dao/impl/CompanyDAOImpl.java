package com.EmployeeManagement.dao.impl;

import com.EmployeeManagement.dao.CompanyDAO;
import com.EmployeeManagement.entity.Company;
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
public class CompanyDAOImpl implements CompanyDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Company save(Company company) {
        if (company.getId() == null) {
            String sql = "INSERT INTO company (name, foundedYear, mission, vision, companyValues, locations, perks, testimonials, cultureHighlights, faqs, contactInfo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, company.getName());
                ps.setObject(2, company.getFoundedYear());
                ps.setString(3, company.getMission());
                ps.setString(4, company.getVision());
                ps.setString(5, company.getCompanyValues());
                ps.setString(6, company.getLocations());
                ps.setString(7, company.getPerks());
                ps.setString(8, company.getTestimonials());
                ps.setString(9, company.getCultureHighlights());
                ps.setString(10, company.getFaqs());
                ps.setString(11, company.getContactInfo());
                return ps;
            }, keyHolder);

            company.setId(keyHolder.getKey().longValue());
        } else {
            String sql = "UPDATE company SET name = ?, foundedYear = ?, mission = ?, vision = ?, companyValues = ?, locations = ?, perks = ?, testimonials = ?, cultureHighlights = ?, faqs = ?, contactInfo = ? WHERE id = ?";
            jdbcTemplate.update(sql,
                company.getName(),
                company.getFoundedYear(),
                company.getMission(),
                company.getVision(),
                company.getCompanyValues(),
                company.getLocations(),
                company.getPerks(),
                company.getTestimonials(),
                company.getCultureHighlights(),
                company.getFaqs(),
                company.getContactInfo(),
                company.getId()
            );
        }
        return company;
    }

    @Override
    public List<Company> findAll() {
        String sql = "SELECT * FROM company";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Company.class));
    }

    @Override
    public Optional<Company> findById(Long id) {
        String sql = "SELECT * FROM company WHERE id = ?";
        try {
            Company company = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Company.class), id);
            return Optional.ofNullable(company);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
