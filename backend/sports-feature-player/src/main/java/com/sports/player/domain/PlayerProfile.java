package com.sports.player.domain;

import com.sports.auth.domain.User;
import com.sports.common.domain.BaseEntity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "player_profiles", indexes = {
    @Index(name = "idx_player_user_id", columnList = "user_id"),
    @Index(name = "idx_player_pos_age", columnList = "position, age")
})
public class PlayerProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false)
    private Integer age = 18;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PlayerPosition position = PlayerPosition.STRIKER;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "player_videos", joinColumns = @JoinColumn(name = "player_profile_id"))
    @Column(name = "video_url", length = 500)
    private List<String> videoUrls = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "player_secondary_positions", joinColumns = @JoinColumn(name = "player_profile_id"))
    @Column(name = "secondary_position", length = 50)
    @Enumerated(EnumType.STRING)
    private Set<PlayerPosition> secondaryPositions = new HashSet<>();

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String bio;

    @Column(length = 20)
    private String phone;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "height_cm")
    private Integer heightCm = 175;

    @Column(name = "weight_kg")
    private Integer weightKg = 70;

    @Column(name = "preferred_foot", length = 10)
    private String preferredFoot = "RIGHT";

    @Column(name = "skill_pace")
    private Integer pace = 75;

    @Column(name = "skill_shooting")
    private Integer shooting = 70;

    @Column(name = "skill_passing")
    private Integer passing = 70;

    @Column(name = "skill_dribbling")
    private Integer dribbling = 75;

    @Column(name = "skill_defending")
    private Integer defending = 65;

    @Column(name = "skill_physicality")
    private Integer physicality = 70;

    public PlayerProfile() {}

    public PlayerProfile(User user, String fullName, Integer age, PlayerPosition position, String videoUrl) {
        this.user = user;
        this.fullName = fullName;
        this.age = age != null ? age : 18;
        this.position = position != null ? position : PlayerPosition.STRIKER;
        this.videoUrl = videoUrl;
        if (videoUrl != null && !videoUrl.isBlank()) {
            this.videoUrls.add(videoUrl);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public PlayerPosition getPosition() { return position; }
    public void setPosition(PlayerPosition position) { this.position = position; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public List<String> getVideoUrls() { return videoUrls; }
    public void setVideoUrls(List<String> videoUrls) { this.videoUrls = videoUrls; }

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

    public Set<PlayerPosition> getSecondaryPositions() { return secondaryPositions; }
    public void setSecondaryPositions(Set<PlayerPosition> secondaryPositions) { this.secondaryPositions = secondaryPositions; }
}
