package com.sports.club.dto;

import com.sports.club.domain.ClubProfile;

public class ClubProfileDto {
    private Long id;
    private Long userId;
    private String clubName;
    private String logoUrl;
    private String cityCountry;
    private String contactPhone;
    private String email;
    private String websiteOrPageUrl;
    private String description;
    private String leagueLevel;
    private boolean activelyRecruiting;

    public static class PublicProfile {
        private Long id; private String clubName; private String websiteOrPageUrl; private String description;
        private String logoUrl; private String cityCountry; private String leagueLevel; private boolean activelyRecruiting;
        public static PublicProfile fromEntity(ClubProfile e) {
            PublicProfile p=new PublicProfile(); p.id=e.getId(); p.clubName=e.getClubName(); p.websiteOrPageUrl=e.getWebsiteOrPageUrl();
            p.description=e.getDescription(); p.logoUrl=e.getLogoUrl(); p.cityCountry=e.getCityCountry();
            p.leagueLevel=e.getLeagueLevel(); p.activelyRecruiting=e.isActivelyRecruiting(); return p;
        }
        public Long getId(){return id;} public String getClubName(){return clubName;} public String getWebsiteOrPageUrl(){return websiteOrPageUrl;}
        public String getDescription(){return description;} public String getLogoUrl(){return logoUrl;} public String getCityCountry(){return cityCountry;}
        public String getLeagueLevel(){return leagueLevel;} public boolean isActivelyRecruiting(){return activelyRecruiting;}
    }

    public ClubProfileDto() {}

    public static ClubProfileDto fromEntity(ClubProfile entity) {
        ClubProfileDto dto = new ClubProfileDto();
        dto.setId(entity.getId());
        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getId());
            dto.setEmail(entity.getUser().getEmail());
        }
        dto.setClubName(entity.getClubName());
        dto.setLogoUrl(entity.getLogoUrl());
        dto.setCityCountry(entity.getCityCountry());
        dto.setContactPhone(entity.getContactPhone());
        dto.setWebsiteOrPageUrl(entity.getWebsiteOrPageUrl());
        dto.setDescription(entity.getDescription());
        dto.setLeagueLevel(entity.getLeagueLevel());
        dto.setActivelyRecruiting(entity.isActivelyRecruiting());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getClubName() { return clubName; }
    public void setClubName(String clubName) { this.clubName = clubName; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getCityCountry() { return cityCountry; }
    public void setCityCountry(String cityCountry) { this.cityCountry = cityCountry; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getWebsiteOrPageUrl() { return websiteOrPageUrl; }
    public void setWebsiteOrPageUrl(String websiteOrPageUrl) { this.websiteOrPageUrl = websiteOrPageUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLeagueLevel() { return leagueLevel; }
    public void setLeagueLevel(String leagueLevel) { this.leagueLevel = leagueLevel; }
    public boolean isActivelyRecruiting() { return activelyRecruiting; }
    public void setActivelyRecruiting(boolean activelyRecruiting) { this.activelyRecruiting = activelyRecruiting; }
}
