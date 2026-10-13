package com.sports.club.dto;

import com.sports.club.domain.ClubProfile;

public class ClubProfileDto {
    private Long id;
    private Long userId;
    private String clubName;
    private String logoUrl;
    private String cityCountry;
    private String email;

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

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
