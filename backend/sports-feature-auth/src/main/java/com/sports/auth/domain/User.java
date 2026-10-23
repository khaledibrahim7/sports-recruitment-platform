package com.sports.auth.domain;

import com.sports.common.domain.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email")
})
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "terms_accepted", nullable = false)
    private boolean termsAccepted;

    @Column(name = "terms_version", length = 40)
    private String termsVersion;

    @Column(name = "terms_accepted_at")
    private java.time.Instant termsAcceptedAt;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = true;

    @Column(name = "organization_approved", nullable = false)
    private boolean organizationApproved = true;

    @Column(name = "organization_approved_at")
    private java.time.Instant organizationApprovedAt;

    @Column(name = "organization_approved_by")
    private Long organizationApprovedBy;

    @Column(name = "guardian_consent_verified", nullable = false)
    private boolean guardianConsentVerified = true;

    @Column(name = "guardian_consent_verified_at")
    private java.time.Instant guardianConsentVerifiedAt;

    @Column(name = "verification_email_sent_at")
    private java.time.Instant verificationEmailSentAt;

    @Column(name = "guardian_consent_email_sent_at")
    private java.time.Instant guardianConsentEmailSentAt;

    public User() {}

    public User(String email, String passwordHash, Role role, String fullName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.fullName = fullName;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public boolean isTermsAccepted() { return termsAccepted; }
    public void setTermsAccepted(boolean termsAccepted) { this.termsAccepted = termsAccepted; }
    public String getTermsVersion() { return termsVersion; }
    public void setTermsVersion(String termsVersion) { this.termsVersion = termsVersion; }
    public java.time.Instant getTermsAcceptedAt() { return termsAcceptedAt; }
    public void setTermsAcceptedAt(java.time.Instant termsAcceptedAt) { this.termsAcceptedAt = termsAcceptedAt; }
    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }
    public boolean isOrganizationApproved() { return organizationApproved; }
    public void setOrganizationApproved(boolean organizationApproved) { this.organizationApproved = organizationApproved; }
    public java.time.Instant getOrganizationApprovedAt() { return organizationApprovedAt; }
    public void setOrganizationApprovedAt(java.time.Instant organizationApprovedAt) { this.organizationApprovedAt = organizationApprovedAt; }
    public Long getOrganizationApprovedBy() { return organizationApprovedBy; }
    public void setOrganizationApprovedBy(Long organizationApprovedBy) { this.organizationApprovedBy = organizationApprovedBy; }
    public boolean isGuardianConsentVerified() { return guardianConsentVerified; }
    public void setGuardianConsentVerified(boolean guardianConsentVerified) { this.guardianConsentVerified = guardianConsentVerified; }
    public java.time.Instant getGuardianConsentVerifiedAt() { return guardianConsentVerifiedAt; }
    public void setGuardianConsentVerifiedAt(java.time.Instant guardianConsentVerifiedAt) { this.guardianConsentVerifiedAt = guardianConsentVerifiedAt; }
    public java.time.Instant getVerificationEmailSentAt() { return verificationEmailSentAt; }
    public void setVerificationEmailSentAt(java.time.Instant verificationEmailSentAt) { this.verificationEmailSentAt = verificationEmailSentAt; }
    public java.time.Instant getGuardianConsentEmailSentAt() { return guardianConsentEmailSentAt; }
    public void setGuardianConsentEmailSentAt(java.time.Instant guardianConsentEmailSentAt) { this.guardianConsentEmailSentAt = guardianConsentEmailSentAt; }
}
