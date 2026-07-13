package com.EmployeeManagement.mapper;

import com.EmployeeManagement.dto.CompanyDTO;
import com.EmployeeManagement.entity.Company;
import com.EmployeeManagement.entity.Location;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CompanyMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public CompanyDTO toDTO(Company company) {
        if (company == null) return null;

        CompanyDTO dto = new CompanyDTO();
        dto.setId(company.getId());
        dto.setName(company.getName());
        dto.setFounded(company.getFoundedYear());
        dto.setMission(company.getMission());
        dto.setVision(company.getVision());

        try {
            if (company.getCompanyValues() != null) {
                dto.setValues(objectMapper.readValue(company.getCompanyValues(), new TypeReference<List<String>>() {}));
            }
            if (company.getLocations() != null) {
                dto.setLocations(objectMapper.readValue(company.getLocations(), new TypeReference<List<Location>>() {}));
            }
            if (company.getPerks() != null) {
                dto.setPerks(objectMapper.readValue(company.getPerks(), new TypeReference<List<CompanyDTO.PerkDTO>>() {}));
            }
            if (company.getTestimonials() != null) {
                dto.setTestimonials(objectMapper.readValue(company.getTestimonials(), new TypeReference<List<CompanyDTO.TestimonialDTO>>() {}));
            }
            if (company.getCultureHighlights() != null) {
                dto.setCultureHighlights(objectMapper.readValue(company.getCultureHighlights(), new TypeReference<List<CompanyDTO.CultureHighlightDTO>>() {}));
            }
            if (company.getFaqs() != null) {
                dto.setFaqs(objectMapper.readValue(company.getFaqs(), new TypeReference<List<CompanyDTO.FAQDTO>>() {}));
            }
            if (company.getContactInfo() != null) {
                dto.setContactInfo(objectMapper.readValue(company.getContactInfo(), CompanyDTO.ContactInfoDTO.class));
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            // Fallback to empty lists if parsing fails
            dto.setValues(new ArrayList<>());
            dto.setLocations(new ArrayList<>());
            dto.setPerks(new ArrayList<>());
            dto.setTestimonials(new ArrayList<>());
            dto.setCultureHighlights(new ArrayList<>());
            dto.setFaqs(new ArrayList<>());
        }

        return dto;
    }

    public Company toEntity(CompanyDTO dto) {
        if (dto == null) return null;

        Company company = new Company();
        company.setId(dto.getId());
        company.setName(dto.getName());
        company.setFoundedYear(dto.getFounded());
        company.setMission(dto.getMission());
        company.setVision(dto.getVision());

        try {
            if (dto.getValues() != null) {
                company.setCompanyValues(objectMapper.writeValueAsString(dto.getValues()));
            }
            if (dto.getLocations() != null) {
                company.setLocations(objectMapper.writeValueAsString(dto.getLocations()));
            }
            if (dto.getPerks() != null) {
                company.setPerks(objectMapper.writeValueAsString(dto.getPerks()));
            }
            if (dto.getTestimonials() != null) {
                company.setTestimonials(objectMapper.writeValueAsString(dto.getTestimonials()));
            }
            if (dto.getCultureHighlights() != null) {
                company.setCultureHighlights(objectMapper.writeValueAsString(dto.getCultureHighlights()));
            }
            if (dto.getFaqs() != null) {
                company.setFaqs(objectMapper.writeValueAsString(dto.getFaqs()));
            }
            if (dto.getContactInfo() != null) {
                company.setContactInfo(objectMapper.writeValueAsString(dto.getContactInfo()));
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return company;
    }
}
