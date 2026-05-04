package com.EmployeeManagement.service;

import com.EmployeeManagement.dto.CompanyDTO;
import com.EmployeeManagement.entity.Company;
import com.EmployeeManagement.entity.Location;
import com.EmployeeManagement.entity.Role;
import com.EmployeeManagement.mapper.CompanyMapper;
import com.EmployeeManagement.dao.CompanyDAO;
import com.EmployeeManagement.dao.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CompanyService {

    @Autowired
    private CompanyDAO companyDAO;

    @Autowired
    private UserDAO userDAO;
    

    @Autowired
    private CompanyMapper companyMapper;

    public Map<String, Object> getCompanyDetails() {

        Map<String, Object> res = new HashMap<>();

        List<Company> companies = companyDAO.findAll();

        Company company;
        if (companies != null && !companies.isEmpty()) {
            company = companies.get(0);
        } else {
            company = new Company();
            company.setName("TechFlow Solutions");
            company.setFoundedYear(2018);
        }

        // Initialize sample data if empty
        if (company.getMission() == null || company.getMission().isEmpty()) {
            company.setMission("To empower businesses through seamless technological integration and innovative software solutions.");
            company.setVision("To be the global leader in providing intuitive, high-performance tools for the modern workforce.");
            company.setValues(Arrays.asList("Innovation First", "User Centricity", "Extreme Ownership", "Continuous Learning"));
            
            // Add Perks
            company.setPerks(java.util.Arrays.asList(
                new Company.Perk("fa-laptop-code", "Premium Gear", "Latest MacBook Pro or high-end workstation of your choice."),
                new Company.Perk("fa-clock", "Flexible Hours", "Work whenever you are most productive, with a remote-first culture."),
                new Company.Perk("fa-graduation-cap", "Learning Budget", "$2000 annual budget for courses, books, and conferences."),
                new Company.Perk("fa-heartbeat", "Health & Wellness", "Comprehensive health insurance and gym memberships.")
            ));

            // Add Testimonials
            company.setTestimonials(Arrays.asList(
                new Company.Testimonial("Sarah Johnson", "Senior Developer", "The culture here is unmatched. I've grown more in 2 years than in my entire previous career.", "/uploads/testimonial1.jpg"),
                new Company.Testimonial("Marcus Chen", "Product Designer", "Innovation isn't just a buzzword here; it's how we breathe. Every pixel we place matters.", "/uploads/testimonial2.jpg")
            ));

            // Add FAQs
            company.setFaqs(Arrays.asList(
                new Company.FAQ("How long is the hiring process?", "Typically 2-3 weeks from the first interview to an offer."),
                new Company.FAQ("Do you offer relocation assistance?", "Yes, we provide comprehensive relocation packages for on-site roles."),
                new Company.FAQ("What is the tech stack?", "We primarily use Spring Boot, MySQL, and Angular/React.")
            ));

            company.setContactInfo(new Company.ContactInfo("hello@techflow.com", "+1 (555) 123-4567", "linkedin.com/company/techflow", "twitter.com/techflow", "instagram.com/techflow_life"));
            
            companyDAO.save(company);
        }

        res.put("company", companyMapper.toDTO(company));
        res.put("employeeCount", userDAO.countByRole(Role.EMPLOYEE));
        res.put("adminCount", userDAO.countByRole(Role.ADMIN));
        res.put("totalUsers", userDAO.findAll().size());

        int locationCount = 0;
        if (company.getLocations() != null) {
            locationCount = company.getLocations().size();
        }

        res.put("locationCount", locationCount);
        res.put("locations", company.getLocations());

        return res;
    }

    public CompanyDTO saveCompany(CompanyDTO companyDto) {
        Company company = companyMapper.toEntity(companyDto);
        return companyMapper.toDTO(companyDAO.save(company));
    }

    public Company addLocation(Location loc) {

        List<Company> companies = companyDAO.findAll();

        if (companies == null || companies.isEmpty()) {
            throw new RuntimeException("Company not found");
        }

        Company company = companies.get(0);

        if (company.getLocations() == null) {
            company.setLocations(new ArrayList<>());
        }

        company.getLocations().add(loc);

        return companyDAO.save(company);
    }

    public Company updateLocations(List<Location> newLocations) {

        List<Company> companies = companyDAO.findAll();

        if (companies == null || companies.isEmpty()) {
            throw new RuntimeException("Company not found");
        }

        Company company = companies.get(0);

        company.setLocations(newLocations);

        return companyDAO.save(company);
    }

    public Company deleteLocation(String locationId) {

        List<Company> companies = companyDAO.findAll();

        if (companies == null || companies.isEmpty()) {
            throw new RuntimeException("Company not found");
        }

        Company company = companies.get(0);

        if (company.getLocations() != null) {

            List<Location> updatedList = new ArrayList<>();

            for (Location l : company.getLocations()) {
                if (l.getId() == null || !l.getId().equals(locationId)) {
                    updatedList.add(l);
                }
            }

            company.setLocations(updatedList);
        }

        return companyDAO.save(company);
    }
}