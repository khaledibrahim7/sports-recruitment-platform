package com.sports.player.dto;

import com.sports.player.domain.PlayerPosition;

import java.util.ArrayList;
import java.util.List;

public class PlayerProfileUpdateRequest {
    private String fullName;
    private Integer age;
    private java.time.LocalDate birthDate;
    private String nationality;
    private String cityCountry;
    private String currentTeam;
    private String competitionLevel;
    private String playerGoal;
    private Boolean availableForTransfer;
    private PlayerPosition position;
    private List<PlayerPosition> secondaryPositions = new ArrayList<>();
    private String videoUrl;
    private List<String> videoUrls = new ArrayList<>();
    private List<String> uploadedVideoUrls = new ArrayList<>();
    private List<String> removedUploadedVideoUrls = new ArrayList<>();
    private String bio;
    private String phone;
    private String avatarUrl;

    // Physical Specs & Radar Skills
    private Integer heightCm;
    private Integer weightKg;
    private String preferredFoot;
    private Integer pace;
    private Integer shooting;
    private Integer passing;
    private Integer dribbling;
    private Integer defending;
    private Integer physicality;

    public PlayerProfileUpdateRequest() {}

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public java.time.LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(java.time.LocalDate birthDate) { this.birthDate = birthDate; }
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    public String getCityCountry() { return cityCountry; }
    public void setCityCountry(String cityCountry) { this.cityCountry = cityCountry; }
    public String getCurrentTeam() { return currentTeam; }
    public void setCurrentTeam(String currentTeam) { this.currentTeam = currentTeam; }
    public String getCompetitionLevel() { return competitionLevel; }
    public void setCompetitionLevel(String competitionLevel) { this.competitionLevel = competitionLevel; }
    public String getPlayerGoal() { return playerGoal; }
    public void setPlayerGoal(String playerGoal) { this.playerGoal = playerGoal; }
    public Boolean getAvailableForTransfer() { return availableForTransfer; }
    public void setAvailableForTransfer(Boolean availableForTransfer) { this.availableForTransfer = availableForTransfer; }

    public PlayerPosition getPosition() { return position; }
    public void setPosition(PlayerPosition position) { this.position = position; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public List<String> getVideoUrls() { return videoUrls; }
    public void setVideoUrls(List<String> videoUrls) { this.videoUrls = videoUrls; }
    public List<String> getUploadedVideoUrls() { return uploadedVideoUrls; }
    public void setUploadedVideoUrls(List<String> uploadedVideoUrls) { this.uploadedVideoUrls = uploadedVideoUrls; }
    public List<String> getRemovedUploadedVideoUrls() { return removedUploadedVideoUrls; }
    public void setRemovedUploadedVideoUrls(List<String> removedUploadedVideoUrls) { this.removedUploadedVideoUrls = removedUploadedVideoUrls; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public Integer getHeightCm() { return heightCm; }
    public void setHeightCm(Integer heightCm) { this.heightCm = heightCm; }

    public Integer getWeightKg() { return weightKg; }
    public void setWeightKg(Integer weightKg) { this.weightKg = weightKg; }

    public String getPreferredFoot() { return preferredFoot; }
    public void setPreferredFoot(String preferredFoot) { this.preferredFoot = preferredFoot; }

    public Integer getPace() { return pace; }
    public void setPace(Integer pace) { this.pace = pace; }

    public Integer getShooting() { return shooting; }
    public void setShooting(Integer shooting) { this.shooting = shooting; }

    public Integer getPassing() { return passing; }
    public void setPassing(Integer passing) { this.passing = passing; }

    public Integer getDribbling() { return dribbling; }
    public void setDribbling(Integer dribbling) { this.dribbling = dribbling; }

    public Integer getDefending() { return defending; }
    public void setDefending(Integer defending) { this.defending = defending; }

    public Integer getPhysicality() { return physicality; }
    public void setPhysicality(Integer physicality) { this.physicality = physicality; }

    public List<PlayerPosition> getSecondaryPositions() { return secondaryPositions; }
    public void setSecondaryPositions(List<PlayerPosition> secondaryPositions) { this.secondaryPositions = secondaryPositions; }
}
