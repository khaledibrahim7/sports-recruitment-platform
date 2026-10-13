package com.sports.player.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "trial_offers", indexes = {
    @Index(name = "idx_trial_player_id", columnList = "player_id"),
    @Index(name = "idx_trial_scout_user_id", columnList = "scout_user_id")
})
public class TrialOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scout_user_id", nullable = false)
    private Long scoutUserId;

    @Column(name = "scout_name", nullable = false)
    private String scoutName;

    @Column(name = "scout_role", nullable = false)
    private String scoutRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerProfile player;

    @Column(name = "trial_date", nullable = false)
    private String trialDate;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "details", length = 1000)
    private String details;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TrialOfferStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public TrialOffer() {
    }

    public TrialOffer(Long scoutUserId, String scoutName, String scoutRole, PlayerProfile player, String trialDate, String location, String details) {
        this.scoutUserId = scoutUserId;
        this.scoutName = scoutName;
        this.scoutRole = scoutRole;
        this.player = player;
        this.trialDate = trialDate;
        this.location = location;
        this.details = details;
        this.status = TrialOfferStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = TrialOfferStatus.PENDING;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getScoutUserId() {
        return scoutUserId;
    }

    public void setScoutUserId(Long scoutUserId) {
        this.scoutUserId = scoutUserId;
    }

    public String getScoutName() {
        return scoutName;
    }

    public void setScoutName(String scoutName) {
        this.scoutName = scoutName;
    }

    public String getScoutRole() {
        return scoutRole;
    }

    public void setScoutRole(String scoutRole) {
        this.scoutRole = scoutRole;
    }

    public PlayerProfile getPlayer() {
        return player;
    }

    public void setPlayer(PlayerProfile player) {
        this.player = player;
    }

    public String getTrialDate() {
        return trialDate;
    }

    public void setTrialDate(String trialDate) {
        this.trialDate = trialDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public TrialOfferStatus getStatus() {
        return status;
    }

    public void setStatus(TrialOfferStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
