package com.EmployeeManagement.dao.impl;

import com.EmployeeManagement.dao.CompanyDAO;
import com.EmployeeManagement.entity.Company;
import com.EmployeeManagement.entity.Location;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CompanyDAOImpl implements CompanyDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private final RowMapper<Company> companyRowMapper = new RowMapper<Company>() {
        @Override
        public Company mapRow(ResultSet rs, int rowNum) throws SQLException {
            Company company = new Company();
            company.setId(rs.getString("id"));
            company.setName(rs.getString("name"));
            company.setFoundedYear(rs.getInt("foundedYear"));
            company.setMission(rs.getString("mission"));
            company.setVision(rs.getString("vision"));
            try {
                company.setValues(objectMapper.readValue(rs.getString("companyValues"), new TypeReference<List<String>>() {}));
                company.setLocations(objectMapper.readValue(rs.getString("locations"), new TypeReference<List<Location>>() {}));
                company.setPerks(objectMapper.readValue(rs.getString("perks"), new TypeReference<List<Company.Perk>>() {}));
                company.setTestimonials(objectMapper.readValue(rs.getString("testimonials"), new TypeReference<List<Company.Testimonial>>() {}));
                company.setCultureHighlights(objectMapper.readValue(rs.getString("cultureHighlights"), new TypeReference<List<Company.CultureHighlight>>() {}));
                company.setFaqs(objectMapper.readValue(rs.getString("faqs"), new TypeReference<List<Company.FAQ>>() {}));
                company.setContactInfo(objectMapper.readValue(rs.getString("contactInfo"), Company.ContactInfo.class));
            } catch (Exception e) {
                // Log error or handle empty strings
            }
            return company;
        }
    };

    @Override
    public Company save(Company company) {
        if (company.getId() == null || company.getId().isEmpty()) {
            company.setId(UUID.randomUUID().toString());
            String sql = "INSERT INTO company (id, name, foundedYear, mission, vision, companyValues, locations, perks, testimonials, cultureHighlights, faqs, contactInfo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try {
                jdbcTemplate.update(sql, 
                    company.getId(), 
                    company.getName(), 
                    company.getFoundedYear(), 
                    company.getMission(), 
                    company.getVision(),
                    objectMapper.writeValueAsString(company.getValues()),
                    objectMapper.writeValueAsString(company.getLocations()),
                    objectMapper.writeValueAsString(company.getPerks()),
                    objectMapper.writeValueAsString(company.getTestimonials()),
                    objectMapper.writeValueAsString(company.getCultureHighlights()),
                    objectMapper.writeValueAsString(company.getFaqs()),
                    objectMapper.writeValueAsString(company.getContactInfo())
                );
            } catch (Exception e) {
                throw new RuntimeException("Error saving company", e);
            }
        } else {
            String sql = "UPDATE company SET name = ?, foundedYear = ?, mission = ?, vision = ?, companyValues = ?, locations = ?, perks = ?, testimonials = ?, cultureHighlights = ?, faqs = ?, contactInfo = ? WHERE id = ?";
            try {
                jdbcTemplate.update(sql, 
                    company.getName(), 
                    company.getFoundedYear(), 
                    company.getMission(), 
                    company.getVision(),
                    objectMapper.writeValueAsString(company.getValues()),
                    objectMapper.writeValueAsString(company.getLocations()),
                    objectMapper.writeValueAsString(company.getPerks()),
                    objectMapper.writeValueAsString(company.getTestimonials()),
                    objectMapper.writeValueAsString(company.getCultureHighlights()),
                    objectMapper.writeValueAsString(company.getFaqs()),
                    objectMapper.writeValueAsString(company.getContactInfo()),
                    company.getId()
                );
            } catch (Exception e) {
                throw new RuntimeException("Error updating company", e);
            }
        }
        return company;
    }

    @Override
    public List<Company> findAll() {
        String sql = "SELECT * FROM company";
        return jdbcTemplate.query(sql, companyRowMapper);
    }

    @Override
    public Optional<Company> findById(String id) {
        String sql = "SELECT * FROM company WHERE id = ?";
        try {
            Company company = jdbcTemplate.queryForObject(sql, companyRowMapper, id);
            return Optional.ofNullable(company);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
