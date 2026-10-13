package com.sports.player.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "player_bookmarks",
    uniqueConstraints = @UniqueConstraint(columnNames = {"scout_user_id", "player_id"}),
    indexes = {
        @Index(name = "idx_scout_user_id", columnList = "scout_user_id")
    }
)
public class PlayerBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scout_user_id", nullable = false)
    private Long scoutUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerProfile player;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public PlayerBookmark() {
    }

    public PlayerBookmark(Long scoutUserId, PlayerProfile player) {
        this.scoutUserId = scoutUserId;
        this.player = player;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
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

    public PlayerProfile getPlayer() {
        return player;
    }

    public void setPlayer(PlayerProfile player) {
        this.player = player;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
