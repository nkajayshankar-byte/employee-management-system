package com.EmployeeManagement.entity;



public class Company {

    private Long id;
    private String name;
    private int foundedYear;
    private String mission;
    private String vision;
    
    // These will be stored as JSON strings in MySQL LONGTEXT columns
    private String companyValues; // Changed from List<String> to String for DB storage, logic handles JSON
    private String locations;     // Changed from List<Location> to String
    private String perks;         // Changed from List<Perk> to String
    private String testimonials;  // Changed from List<Testimonial> to String
    private String cultureHighlights; // Changed from List<CultureHighlight> to String
    private String faqs;          // Changed from List<FAQ> to String
    private String contactInfo;   // Changed from ContactInfo to String

    public Company() {}

    // Inner Classes (kept for DTO or logic use if needed, but entity stores strings)
    public static class Perk {
        private String icon;
        private String title;
        private String description;
        public Perk() {}
        public Perk(String icon, String title, String description) {
            this.icon = icon; this.title = title; this.description = description;
        }
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class Testimonial {
        private String name;
        private String role;
        private String quote;
        private String imageUrl;
        public Testimonial() {}
        public Testimonial(String name, String role, String quote, String imageUrl) {
            this.name = name; this.role = role; this.quote = quote; this.imageUrl = imageUrl;
        }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getQuote() { return quote; }
        public void setQuote(String quote) { this.quote = quote; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }

    public static class CultureHighlight {
        private String title;
        private String description;
        private String imageUrl;
        public CultureHighlight() {}
        public CultureHighlight(String title, String description, String imageUrl) {
            this.title = title; this.description = description; this.imageUrl = imageUrl;
        }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }

    public static class FAQ {
        private String question;
        private String answer;
        public FAQ() {}
        public FAQ(String question, String answer) {
            this.question = question; this.answer = answer;
        }
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
    }

    public static class ContactInfo {
        private String email;
        private String phone;
        private String linkedin;
        private String twitter;
        private String instagram;
        public ContactInfo() {}
        public ContactInfo(String email, String phone, String linkedin, String twitter, String instagram) {
            this.email = email; this.phone = phone; this.linkedin = linkedin; this.twitter = twitter; this.instagram = instagram;
        }
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

    // Company Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getFoundedYear() { return foundedYear; }
    public void setFoundedYear(int foundedYear) { this.foundedYear = foundedYear; }
    public String getMission() { return mission; }
    public void setMission(String mission) { this.mission = mission; }
    public String getVision() { return vision; }
    public void setVision(String vision) { this.vision = vision; }

	public String getCompanyValues() {
		return companyValues;
	}

	public void setCompanyValues(String companyValues) {
		this.companyValues = companyValues;
	}

	public String getLocations() {
		return locations;
	}

	public void setLocations(String locations) {
		this.locations = locations;
	}

	public String getPerks() {
		return perks;
	}

	public void setPerks(String perks) {
		this.perks = perks;
	}

	public String getTestimonials() {
		return testimonials;
	}

	public void setTestimonials(String testimonials) {
		this.testimonials = testimonials;
	}

	public String getCultureHighlights() {
		return cultureHighlights;
	}

	public void setCultureHighlights(String cultureHighlights) {
		this.cultureHighlights = cultureHighlights;
	}

	public String getFaqs() {
		return faqs;
	}

	public void setFaqs(String faqs) {
		this.faqs = faqs;
	}

	public String getContactInfo() {
		return contactInfo;
	}

	public void setContactInfo(String contactInfo) {
		this.contactInfo = contactInfo;
	}
}
