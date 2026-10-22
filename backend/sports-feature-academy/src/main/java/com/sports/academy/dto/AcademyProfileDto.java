package com.sports.academy.dto;

import com.sports.academy.domain.AcademyProfile;
import java.util.List;

public class AcademyProfileDto {
    private Long id;
    private Long userId;
    private String academyName;
    private String websiteOrPageUrl;
    private String cityCountry;
    private String contactPhone;
    private String description;
    private String logoUrl;
    private String email;
    private List<String> ageGroups;
    private String trainingType;

    public static class PublicProfile {
        private Long id; private String academyName; private String websiteOrPageUrl; private String cityCountry;
        private String description; private String logoUrl; private List<String> ageGroups; private String trainingType;
        public static PublicProfile fromEntity(AcademyProfile e) {
            PublicProfile p = new PublicProfile(); p.id=e.getId(); p.academyName=e.getAcademyName();
            p.websiteOrPageUrl=e.getWebsiteOrPageUrl(); p.cityCountry=e.getCityCountry(); p.description=e.getDescription();
            p.logoUrl=e.getLogoUrl(); p.ageGroups=e.getAgeGroups(); p.trainingType=e.getTrainingType(); return p;
        }
        public Long getId(){return id;} public String getAcademyName(){return academyName;}
        public String getWebsiteOrPageUrl(){return websiteOrPageUrl;} public String getCityCountry(){return cityCountry;}
        public String getDescription(){return description;} public String getLogoUrl(){return logoUrl;}
        public List<String> getAgeGroups(){return ageGroups;} public String getTrainingType(){return trainingType;}
    }

    public AcademyProfileDto() {}

    public static AcademyProfileDto fromEntity(AcademyProfile entity) {
        AcademyProfileDto dto = new AcademyProfileDto();
        dto.setId(entity.getId());
        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getId());
            dto.setEmail(entity.getUser().getEmail());
        }
        dto.setAcademyName(entity.getAcademyName());
        dto.setWebsiteOrPageUrl(entity.getWebsiteOrPageUrl());
        dto.setCityCountry(entity.getCityCountry());
        dto.setContactPhone(entity.getContactPhone());
        dto.setDescription(entity.getDescription());
        dto.setLogoUrl(entity.getLogoUrl());
        dto.setAgeGroups(entity.getAgeGroups());
        dto.setTrainingType(entity.getTrainingType());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getAcademyName() { return academyName; }
    public void setAcademyName(String academyName) { this.academyName = academyName; }

    public String getWebsiteOrPageUrl() { return websiteOrPageUrl; }
    public void setWebsiteOrPageUrl(String websiteOrPageUrl) { this.websiteOrPageUrl = websiteOrPageUrl; }
    public String getCityCountry() { return cityCountry; }
    public void setCityCountry(String cityCountry) { this.cityCountry = cityCountry; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public List<String> getAgeGroups() { return ageGroups; }
    public void setAgeGroups(List<String> ageGroups) { this.ageGroups = ageGroups; }
    public String getTrainingType() { return trainingType; }
    public void setTrainingType(String trainingType) { this.trainingType = trainingType; }
}
