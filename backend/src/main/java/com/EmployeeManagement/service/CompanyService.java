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

        CompanyDTO companyDto;
        if (companies != null && !companies.isEmpty()) {
            companyDto = companyMapper.toDTO(companies.get(0));
        } else {
            // Initialize sample data via DTO then save to Entity
            companyDto = new CompanyDTO();
            companyDto.setName("TechFlow Solutions");
            companyDto.setFounded(2018);
            companyDto.setMission("To empower businesses through seamless technological integration and innovative software solutions.");
            companyDto.setVision("To be the global leader in providing intuitive, high-performance tools for the modern workforce.");
            
            List<String> values = new ArrayList<>();
            values.add("Innovation First");
            values.add("User Centricity");
            values.add("Extreme Ownership");
            values.add("Continuous Learning");
            companyDto.setValues(values);

            List<CompanyDTO.PerkDTO> perks = new ArrayList<>();
            perks.add(createPerk("fa-laptop-code", "Premium Gear", "Latest MacBook Pro or high-end workstation of your choice."));
            perks.add(createPerk("fa-clock", "Flexible Hours", "Work whenever you are most productive, with a remote-first culture."));
            perks.add(createPerk("fa-graduation-cap", "Learning Budget", "$2000 annual budget for courses, books, and conferences."));
            perks.add(createPerk("fa-heartbeat", "Health & Wellness", "Comprehensive health insurance and gym memberships."));
            companyDto.setPerks(perks);

            List<CompanyDTO.TestimonialDTO> testimonials = new ArrayList<>();
            testimonials.add(createTestimonial("Sarah Johnson", "Senior Developer", "The culture here is unmatched. I've grown more in 2 years than in my entire previous career.", "/uploads/testimonial1.jpg"));
            testimonials.add(createTestimonial("Marcus Chen", "Product Designer", "Innovation isn't just a buzzword here; it's how we breathe. Every pixel we place matters.", "/uploads/testimonial2.jpg"));
            companyDto.setTestimonials(testimonials);

            List<CompanyDTO.FAQDTO> faqs = new ArrayList<>();
            faqs.add(createFAQ("How long is the hiring process?", "Typically 2-3 weeks from the first interview to an offer."));
            faqs.add(createFAQ("Do you offer relocation assistance?", "Yes, we provide comprehensive relocation packages for on-site roles."));
            faqs.add(createFAQ("What is the tech stack?", "We primarily use Spring Boot, MySQL, and Angular/React."));
            companyDto.setFaqs(faqs);

            CompanyDTO.ContactInfoDTO contact = new CompanyDTO.ContactInfoDTO();
            contact.setEmail("hello@techflow.com");
            contact.setPhone("+1 (555) 123-4567");
            contact.setLinkedin("linkedin.com/company/techflow");
            contact.setTwitter("twitter.com/techflow");
            contact.setInstagram("instagram.com/techflow_life");
            companyDto.setContactInfo(contact);

            Company savedEntity = companyDAO.save(companyMapper.toEntity(companyDto));
            companyDto = companyMapper.toDTO(savedEntity);
        }

        res.put("company", companyDto);
        res.put("employeeCount", userDAO.countByRole(Role.EMPLOYEE));
        res.put("adminCount", userDAO.countByRole(Role.ADMIN));
        res.put("totalUsers", userDAO.findAll().size());

        int locationCount = (companyDto.getLocations() != null) ? companyDto.getLocations().size() : 0;
        res.put("locationCount", locationCount);
        res.put("locations", companyDto.getLocations());

        return res;
    }

    private CompanyDTO.PerkDTO createPerk(String icon, String title, String desc) {
        CompanyDTO.PerkDTO p = new CompanyDTO.PerkDTO();
        p.setIcon(icon); p.setTitle(title); p.setDescription(desc);
        return p;
    }

    private CompanyDTO.TestimonialDTO createTestimonial(String name, String role, String quote, String img) {
        CompanyDTO.TestimonialDTO t = new CompanyDTO.TestimonialDTO();
        t.setName(name); t.setRole(role); t.setQuote(quote); t.setImageUrl(img);
        return t;
    }

    private CompanyDTO.FAQDTO createFAQ(String q, String a) {
        CompanyDTO.FAQDTO f = new CompanyDTO.FAQDTO();
        f.setQuestion(q); f.setAnswer(a);
        return f;
    }

    public CompanyDTO saveCompany(CompanyDTO companyDto) {
        Company company = companyMapper.toEntity(companyDto);
        return companyMapper.toDTO(companyDAO.save(company));
    }

    public Company addLocation(Location loc) {
        List<Company> companies = companyDAO.findAll();
        if (companies == null || companies.isEmpty()) throw new RuntimeException("Company not found");
        
        CompanyDTO dto = companyMapper.toDTO(companies.get(0));
        if (dto.getLocations() == null) dto.setLocations(new ArrayList<>());
        dto.getLocations().add(loc);
        
        return companyDAO.save(companyMapper.toEntity(dto));
    }

    public Company updateLocations(List<Location> newLocations) {
        List<Company> companies = companyDAO.findAll();
        if (companies == null || companies.isEmpty()) throw new RuntimeException("Company not found");
        
        CompanyDTO dto = companyMapper.toDTO(companies.get(0));
        dto.setLocations(newLocations);
        
        return companyDAO.save(companyMapper.toEntity(dto));
    }

    public Company deleteLocation(Long locationId) {
        List<Company> companies = companyDAO.findAll();
        if (companies == null || companies.isEmpty()) throw new RuntimeException("Company not found");
        
        CompanyDTO dto = companyMapper.toDTO(companies.get(0));
        if (dto.getLocations() != null) {
            dto.getLocations().removeIf(l -> l.getId() != null && l.getId().equals(locationId));
        }
        
        return companyDAO.save(companyMapper.toEntity(dto));
    }
}