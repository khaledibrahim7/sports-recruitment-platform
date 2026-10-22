package com.sports.player.dto;

import com.sports.player.domain.PlayerPosition;
import com.sports.player.domain.PlayerProfile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlayerProfileDto {
    private Long id;
    private Long userId;
    private String fullName;
    private Integer age;
    private java.time.LocalDate birthDate;
    private String nationality;
    private String cityCountry;
    private String currentTeam;
    private String competitionLevel;
    private String playerGoal;
    private boolean availableForTransfer;
    private String guardianName;
    private String guardianPhone;
    private java.time.Instant guardianConsentAt;
    private PlayerPosition position;
    private Set<PlayerPosition> secondaryPositions = new HashSet<>();
    private String videoUrl;
    private List<String> videoUrls = new ArrayList<>();
    private List<String> uploadedVideoUrls = new ArrayList<>();
    private String bio;
    private String phone;
    private String email;
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

    public PlayerProfileDto() {}

    public static PlayerProfileDto fromEntity(PlayerProfile entity) {
        PlayerProfileDto dto = new PlayerProfileDto();
        dto.setId(entity.getId());
        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getId());
            dto.setEmail(entity.getUser().getEmail());
        }
        dto.setFullName(entity.getFullName());
        dto.setAge(entity.getAge());
        dto.setBirthDate(entity.getBirthDate());
        dto.setNationality(entity.getNationality());
        dto.setCityCountry(entity.getCityCountry());
        dto.setCurrentTeam(entity.getCurrentTeam());
        dto.setCompetitionLevel(entity.getCompetitionLevel());
        dto.setPlayerGoal(entity.getPlayerGoal());
        dto.setAvailableForTransfer(entity.isAvailableForTransfer());
        dto.setGuardianName(entity.getGuardianName());
        dto.setGuardianPhone(entity.getGuardianPhone());
        dto.setGuardianConsentAt(entity.getGuardianConsentAt());
        dto.setPosition(entity.getPosition());
        dto.setSecondaryPositions(entity.getSecondaryPositions() != null ? new HashSet<>(entity.getSecondaryPositions()) : new HashSet<>());
        dto.setVideoUrl(entity.getVideoUrl());
        dto.setVideoUrls(entity.getVideoUrls() != null ? new ArrayList<>(entity.getVideoUrls()) : new ArrayList<>());
        dto.setUploadedVideoUrls(dto.getVideoUrls().stream().filter(url -> url != null && url.startsWith("platform-video:")).collect(java.util.stream.Collectors.toList()));
        dto.setBio(entity.getBio());
        dto.setPhone(entity.getPhone());
        dto.setAvatarUrl(entity.getAvatarUrl());

        dto.setHeightCm(entity.getHeightCm());
        dto.setWeightKg(entity.getWeightKg());
        dto.setPreferredFoot(entity.getPreferredFoot());
        dto.setPace(entity.getPace());
        dto.setShooting(entity.getShooting());
        dto.setPassing(entity.getPassing());
        dto.setDribbling(entity.getDribbling());
        dto.setDefending(entity.getDefending());
        dto.setPhysicality(entity.getPhysicality());

        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

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
    public boolean isAvailableForTransfer() { return availableForTransfer; }
    public void setAvailableForTransfer(boolean availableForTransfer) { this.availableForTransfer = availableForTransfer; }
    public String getGuardianName() { return guardianName; }
    public void setGuardianName(String guardianName) { this.guardianName = guardianName; }
    public String getGuardianPhone() { return guardianPhone; }
    public void setGuardianPhone(String guardianPhone) { this.guardianPhone = guardianPhone; }
    public java.time.Instant getGuardianConsentAt() { return guardianConsentAt; }
    public void setGuardianConsentAt(java.time.Instant guardianConsentAt) { this.guardianConsentAt = guardianConsentAt; }

    public PlayerPosition getPosition() { return position; }
    public void setPosition(PlayerPosition position) { this.position = position; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public List<String> getVideoUrls() { return videoUrls; }
    public void setVideoUrls(List<String> videoUrls) { this.videoUrls = videoUrls; }
    public List<String> getUploadedVideoUrls() { return uploadedVideoUrls; }
    public void setUploadedVideoUrls(List<String> uploadedVideoUrls) { this.uploadedVideoUrls = uploadedVideoUrls; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

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

    public Set<PlayerPosition> getSecondaryPositions() { return secondaryPositions; }
    public void setSecondaryPositions(Set<PlayerPosition> secondaryPositions) { this.secondaryPositions = secondaryPositions; }
}
