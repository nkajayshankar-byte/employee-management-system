package com.EmployeeManagement.mapper;

import com.EmployeeManagement.dto.CompanyDTO;
import com.EmployeeManagement.entity.Company;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class CompanyMapper {

    public CompanyDTO toDTO(Company company) {
        if (company == null) return null;

        CompanyDTO dto = new CompanyDTO();
        dto.setId(company.getId());
        dto.setName(company.getName());
        dto.setFounded(company.getFoundedYear());
        dto.setMission(company.getMission());
        dto.setVision(company.getVision());
        dto.setValues(company.getValues());
        dto.setLocations(company.getLocations());

        if (company.getPerks() != null) {
            dto.setPerks(company.getPerks().stream().map(p -> {
                CompanyDTO.PerkDTO pd = new CompanyDTO.PerkDTO();
                pd.setIcon(p.getIcon());
                pd.setTitle(p.getTitle());
                pd.setDescription(p.getDescription());
                return pd;
            }).collect(Collectors.toList()));
        }

        if (company.getTestimonials() != null) {
            dto.setTestimonials(company.getTestimonials().stream().map(t -> {
                CompanyDTO.TestimonialDTO td = new CompanyDTO.TestimonialDTO();
                td.setName(t.getName());
                td.setRole(t.getRole());
                td.setQuote(t.getQuote());
                td.setImageUrl(t.getImageUrl());
                return td;
            }).collect(Collectors.toList()));
        }

        if (company.getCultureHighlights() != null) {
            dto.setCultureHighlights(company.getCultureHighlights().stream().map(ch -> {
                CompanyDTO.CultureHighlightDTO chd = new CompanyDTO.CultureHighlightDTO();
                chd.setTitle(ch.getTitle());
                chd.setDescription(ch.getDescription());
                chd.setImageUrl(ch.getImageUrl());
                return chd;
            }).collect(Collectors.toList()));
        }

        if (company.getFaqs() != null) {
            dto.setFaqs(company.getFaqs().stream().map(f -> {
                CompanyDTO.FAQDTO fd = new CompanyDTO.FAQDTO();
                fd.setQuestion(f.getQuestion());
                fd.setAnswer(f.getAnswer());
                return fd;
            }).collect(Collectors.toList()));
        }

        if (company.getContactInfo() != null) {
            CompanyDTO.ContactInfoDTO cd = new CompanyDTO.ContactInfoDTO();
            cd.setEmail(company.getContactInfo().getEmail());
            cd.setPhone(company.getContactInfo().getPhone());
            cd.setLinkedin(company.getContactInfo().getLinkedin());
            cd.setTwitter(company.getContactInfo().getTwitter());
            cd.setInstagram(company.getContactInfo().getInstagram());
            dto.setContactInfo(cd);
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
        company.setValues(dto.getValues());
        company.setLocations(dto.getLocations());

        if (dto.getPerks() != null) {
            company.setPerks(dto.getPerks().stream().map(pd -> {
                return new Company.Perk(pd.getIcon(), pd.getTitle(), pd.getDescription());
            }).collect(Collectors.toList()));
        }

        if (dto.getTestimonials() != null) {
            company.setTestimonials(dto.getTestimonials().stream().map(td -> {
                return new Company.Testimonial(td.getName(), td.getRole(), td.getQuote(), td.getImageUrl());
            }).collect(Collectors.toList()));
        }

        if (dto.getCultureHighlights() != null) {
            company.setCultureHighlights(dto.getCultureHighlights().stream().map(chd -> {
                return new Company.CultureHighlight(chd.getTitle(), chd.getDescription(), chd.getImageUrl());
            }).collect(Collectors.toList()));
        }

        if (dto.getFaqs() != null) {
            company.setFaqs(dto.getFaqs().stream().map(fd -> {
                return new Company.FAQ(fd.getQuestion(), fd.getAnswer());
            }).collect(Collectors.toList()));
        }

        if (dto.getContactInfo() != null) {
            company.setContactInfo(new Company.ContactInfo(
                dto.getContactInfo().getEmail(),
                dto.getContactInfo().getPhone(),
                dto.getContactInfo().getLinkedin(),
                dto.getContactInfo().getTwitter(),
                dto.getContactInfo().getInstagram()
            ));
        }

        return company;
    }
}
