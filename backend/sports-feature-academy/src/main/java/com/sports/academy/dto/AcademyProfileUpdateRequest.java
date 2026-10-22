package com.sports.academy.dto;

public class AcademyProfileUpdateRequest {
    private String academyName;
    private String websiteOrPageUrl;
    private String cityCountry;
    private String contactPhone;
    private String description;
    private String logoUrl;
    private java.util.List<String> ageGroups;
    private String trainingType;

    public AcademyProfileUpdateRequest() {}

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
    public java.util.List<String> getAgeGroups() { return ageGroups; }
    public void setAgeGroups(java.util.List<String> ageGroups) { this.ageGroups = ageGroups; }
    public String getTrainingType() { return trainingType; }
    public void setTrainingType(String trainingType) { this.trainingType = trainingType; }
}
