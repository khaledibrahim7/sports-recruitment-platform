package com.sports.club.dto;

public class ClubProfileUpdateRequest {
    private String clubName;
    private String logoUrl;
    private String cityCountry;

    public ClubProfileUpdateRequest() {}

    public String getClubName() { return clubName; }
    public void setClubName(String clubName) { this.clubName = clubName; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getCityCountry() { return cityCountry; }
    public void setCityCountry(String cityCountry) { this.cityCountry = cityCountry; }
}
