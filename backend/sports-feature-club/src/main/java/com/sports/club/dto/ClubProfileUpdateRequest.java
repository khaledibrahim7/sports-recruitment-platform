package com.sports.club.dto;

public class ClubProfileUpdateRequest {
    private String clubName;
    private String logoUrl;
    private String cityCountry;
    private String contactPhone;
    private String websiteOrPageUrl;
    private String description;
    private String leagueLevel;
    private Boolean activelyRecruiting;

    public ClubProfileUpdateRequest() {}

    public String getClubName() { return clubName; }
    public void setClubName(String clubName) { this.clubName = clubName; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getCityCountry() { return cityCountry; }
    public void setCityCountry(String cityCountry) { this.cityCountry = cityCountry; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getWebsiteOrPageUrl() { return websiteOrPageUrl; }
    public void setWebsiteOrPageUrl(String websiteOrPageUrl) { this.websiteOrPageUrl = websiteOrPageUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLeagueLevel() { return leagueLevel; }
    public void setLeagueLevel(String leagueLevel) { this.leagueLevel = leagueLevel; }
    public Boolean getActivelyRecruiting() { return activelyRecruiting; }
    public void setActivelyRecruiting(Boolean activelyRecruiting) { this.activelyRecruiting = activelyRecruiting; }
}
