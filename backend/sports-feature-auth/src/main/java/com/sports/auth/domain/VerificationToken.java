package com.sports.auth.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "account_verification_tokens", indexes = {
        @Index(name = "idx_verification_token_hash", columnList = "token_hash", unique = true),
        @Index(name = "idx_verification_user_type", columnList = "user_id, token_type")
})
public class VerificationToken {
    public enum Type { EMAIL, GUARDIAN }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false, length = 20)
    private Type type;

    @Column(name = "destination_email", nullable = false, length = 254)
    private String destinationEmail;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "consumed_at")
    private Instant consumedAt;

    protected VerificationToken() {}

    public VerificationToken(User user, String tokenHash, Type type, String destinationEmail, Instant expiresAt) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.type = type;
        this.destinationEmail = destinationEmail;
        this.expiresAt = expiresAt;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getTokenHash() { return tokenHash; }
    public Type getType() { return type; }
    public String getDestinationEmail() { return destinationEmail; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getConsumedAt() { return consumedAt; }
    public void setConsumedAt(Instant consumedAt) { this.consumedAt = consumedAt; }
}
