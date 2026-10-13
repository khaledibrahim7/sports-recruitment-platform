package com.sports.academy.domain;

import com.sports.auth.domain.User;
import com.sports.common.domain.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "academy_profiles", indexes = {
    @Index(name = "idx_academy_user_id", columnList = "user_id")
})
public class AcademyProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "academy_name", nullable = false, length = 150)
    private String academyName;

    @Column(name = "website_or_page_url", length = 500)
    private String websiteOrPageUrl;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    public AcademyProfile() {}

    public AcademyProfile(User user, String academyName, String websiteOrPageUrl, String logoUrl) {
        this.user = user;
        this.academyName = academyName;
        this.websiteOrPageUrl = websiteOrPageUrl;
        this.logoUrl = logoUrl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getAcademyName() { return academyName; }
    public void setAcademyName(String academyName) { this.academyName = academyName; }

    public String getWebsiteOrPageUrl() { return websiteOrPageUrl; }
    public void setWebsiteOrPageUrl(String websiteOrPageUrl) { this.websiteOrPageUrl = websiteOrPageUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
}
