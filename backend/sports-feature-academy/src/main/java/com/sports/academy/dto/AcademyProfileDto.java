package com.sports.academy.dto;

import com.sports.academy.domain.AcademyProfile;

public class AcademyProfileDto {
    private Long id;
    private Long userId;
    private String academyName;
    private String websiteOrPageUrl;
    private String description;
    private String logoUrl;
    private String email;

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
        dto.setDescription(entity.getDescription());
        dto.setLogoUrl(entity.getLogoUrl());
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

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
