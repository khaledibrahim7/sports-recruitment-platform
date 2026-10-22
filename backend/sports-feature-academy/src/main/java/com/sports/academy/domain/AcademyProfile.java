package com.sports.academy.domain;

import com.sports.auth.domain.User;
import com.sports.common.domain.BaseEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "city_country", length = 120)
    private String cityCountry;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "academy_age_groups", joinColumns = @JoinColumn(name = "academy_profile_id"))
    @Column(name = "age_group", length = 40)
    private List<String> ageGroups = new ArrayList<>();

    @Column(name = "training_type", length = 80)
    private String trainingType;

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

    public String getCityCountry() { return cityCountry; }
    public void setCityCountry(String cityCountry) { this.cityCountry = cityCountry; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getAgeGroups() { return ageGroups; }
    public void setAgeGroups(List<String> ageGroups) { this.ageGroups = ageGroups != null ? new ArrayList<>(ageGroups) : new ArrayList<>(); }
    public String getTrainingType() { return trainingType; }
    public void setTrainingType(String trainingType) { this.trainingType = trainingType; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
}
