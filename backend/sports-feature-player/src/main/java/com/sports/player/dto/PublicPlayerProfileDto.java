package com.sports.player.dto;

import com.sports.player.domain.PlayerPosition;
import com.sports.player.domain.PlayerProfile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Fields intentionally safe for unauthenticated player discovery. */
public class PublicPlayerProfileDto {
    private Long id;
    private String fullName;
    private Integer age;
    private String cityCountry;
    private String currentTeam;
    private String competitionLevel;
    private PlayerPosition position;
    private Set<PlayerPosition> secondaryPositions = new HashSet<>();
    private List<String> videoUrls = new ArrayList<>();
    private String videoUrl;
    private String bio;
    private String avatarUrl;
    private Integer heightCm;
    private Integer weightKg;
    private String preferredFoot;
    private Integer pace;
    private Integer shooting;
    private Integer passing;
    private Integer dribbling;
    private Integer defending;
    private Integer physicality;

    public static PublicPlayerProfileDto fromEntity(PlayerProfile entity) {
        PublicPlayerProfileDto dto = new PublicPlayerProfileDto();
        dto.id = entity.getId(); dto.fullName = entity.getFullName(); dto.age = entity.getAge();
        dto.cityCountry = entity.getCityCountry(); dto.currentTeam = entity.getCurrentTeam();
        dto.competitionLevel = entity.getCompetitionLevel(); dto.position = entity.getPosition();
        dto.secondaryPositions = entity.getSecondaryPositions() == null ? new HashSet<>() : new HashSet<>(entity.getSecondaryPositions());
        dto.videoUrls = entity.getVideoUrls() == null ? new ArrayList<>() : new ArrayList<>(entity.getVideoUrls());
        if (dto.videoUrls.isEmpty() && entity.getVideoUrl() != null) dto.videoUrls.add(entity.getVideoUrl());
        dto.videoUrl = entity.getVideoUrl();
        dto.bio = entity.getBio(); dto.avatarUrl = entity.getAvatarUrl(); dto.heightCm = entity.getHeightCm();
        dto.weightKg = entity.getWeightKg(); dto.preferredFoot = entity.getPreferredFoot();
        dto.pace = entity.getPace(); dto.shooting = entity.getShooting(); dto.passing = entity.getPassing();
        dto.dribbling = entity.getDribbling(); dto.defending = entity.getDefending(); dto.physicality = entity.getPhysicality();
        return dto;
    }
    public Long getId(){return id;} public String getFullName(){return fullName;} public Integer getAge(){return age;}
    public String getCityCountry(){return cityCountry;} public String getCurrentTeam(){return currentTeam;}
    public String getCompetitionLevel(){return competitionLevel;} public PlayerPosition getPosition(){return position;}
    public Set<PlayerPosition> getSecondaryPositions(){return secondaryPositions;} public List<String> getVideoUrls(){return videoUrls;}
    public String getVideoUrl(){return videoUrl;}
    public String getBio(){return bio;} public String getAvatarUrl(){return avatarUrl;} public Integer getHeightCm(){return heightCm;}
    public Integer getWeightKg(){return weightKg;} public String getPreferredFoot(){return preferredFoot;} public Integer getPace(){return pace;}
    public Integer getShooting(){return shooting;} public Integer getPassing(){return passing;} public Integer getDribbling(){return dribbling;}
    public Integer getDefending(){return defending;} public Integer getPhysicality(){return physicality;}
}
