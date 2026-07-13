package com.EmployeeManagement.dto;

import com.EmployeeManagement.entity.Location;
import java.util.List;

public class CompanyDTO {
    private Long id;
    private String name;
    private int founded;
    private String mission;
    private String vision;
    private List<String> values;
    private List<PerkDTO> perks;
    private List<TestimonialDTO> testimonials;
    private List<FAQDTO> faqs;
    private ContactInfoDTO contactInfo;
    private List<Location> locations;
    private List<CultureHighlightDTO> cultureHighlights;

    // Inner DTOs
    public static class PerkDTO {
        private String icon;
        private String title;
        private String description;
        // Getters and Setters
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class TestimonialDTO {
        private String name;
        private String role;
        private String quote;
        private String imageUrl;
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getQuote() { return quote; }
        public void setQuote(String quote) { this.quote = quote; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }

    public static class CultureHighlightDTO {
        private String title;
        private String description;
        private String imageUrl;
        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }

    public static class FAQDTO {
        private String question;
        private String answer;
        // Getters and Setters
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
    }

    public static class ContactInfoDTO {
        private String email;
        private String phone;
        private String linkedin;
        private String twitter;
        private String instagram;
        // Getters and Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getLinkedin() { return linkedin; }
        public void setLinkedin(String linkedin) { this.linkedin = linkedin; }
        public String getTwitter() { return twitter; }
        public void setTwitter(String twitter) { this.twitter = twitter; }
        public String getInstagram() { return instagram; }
        public void setInstagram(String instagram) { this.instagram = instagram; }
    }

    // Getters and Setters for CompanyDTO
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getFounded() { return founded; }
    public void setFounded(int founded) { this.founded = founded; }
    public String getMission() { return mission; }
    public void setMission(String mission) { this.mission = mission; }
    public String getVision() { return vision; }
    public void setVision(String vision) { this.vision = vision; }
    public List<String> getValues() { return values; }
    public void setValues(List<String> values) { this.values = values; }
    public List<PerkDTO> getPerks() { return perks; }
    public void setPerks(List<PerkDTO> perks) { this.perks = perks; }
    public List<TestimonialDTO> getTestimonials() { return testimonials; }
    public void setTestimonials(List<TestimonialDTO> testimonials) { this.testimonials = testimonials; }
    public List<FAQDTO> getFaqs() { return faqs; }
    public void setFaqs(List<FAQDTO> faqs) { this.faqs = faqs; }
    public ContactInfoDTO getContactInfo() { return contactInfo; }
    public void setContactInfo(ContactInfoDTO contactInfo) { this.contactInfo = contactInfo; }
    public List<Location> getLocations() { return locations; }
    public void setLocations(List<Location> locations) { this.locations = locations; }
    public List<CultureHighlightDTO> getCultureHighlights() { return cultureHighlights; }
    public void setCultureHighlights(List<CultureHighlightDTO> cultureHighlights) { this.cultureHighlights = cultureHighlights; }
}
