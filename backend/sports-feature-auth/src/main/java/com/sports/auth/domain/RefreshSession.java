package com.sports.auth.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "auth_refresh_sessions", indexes = {
        @Index(name = "ix_auth_refresh_user_id", columnList = "user_id"),
        @Index(name = "ix_auth_refresh_expires_at", columnList = "expires_at")
})
public class RefreshSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token_hash", nullable = false, length = 64, unique = true)
    private String tokenHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    protected RefreshSession() {}

    public RefreshSession(Long userId, String tokenHash, Instant createdAt, Instant expiresAt, String userAgent) {
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.userAgent = userAgent;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getTokenHash() { return tokenHash; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
}
