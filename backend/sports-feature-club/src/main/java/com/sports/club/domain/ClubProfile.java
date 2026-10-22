package com.sports.club.domain;

import com.sports.auth.domain.User;
import com.sports.common.domain.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "club_profiles", indexes = {
    @Index(name = "idx_club_user_id", columnList = "user_id")
})
public class ClubProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "club_name", nullable = false, length = 150)
    private String clubName;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "city_country", length = 100)
    private String cityCountry;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "website_or_page_url", length = 500)
    private String websiteOrPageUrl;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "league_level", length = 80)
    private String leagueLevel;

    @Column(name = "actively_recruiting", nullable = false)
    private boolean activelyRecruiting;

    public ClubProfile() {}

    public ClubProfile(User user, String clubName, String logoUrl) {
        this.user = user;
        this.clubName = clubName;
        this.logoUrl = logoUrl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getClubName() { return clubName; }
    public void setClubName(String clubName) { this.clubName = clubName; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getCityCountry() { return cityCountry; }
    public void setCityCountry(String cityCountry) { this.cityCountry = cityCountry; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getWebsiteOrPageUrl() { return websiteOrPageUrl; }
    public void setWebsiteOrPageUrl(String websiteOrPageUrl) { this.websiteOrPageUrl = websiteOrPageUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLeagueLevel() { return leagueLevel; }
    public void setLeagueLevel(String leagueLevel) { this.leagueLevel = leagueLevel; }
    public boolean isActivelyRecruiting() { return activelyRecruiting; }
    public void setActivelyRecruiting(boolean activelyRecruiting) { this.activelyRecruiting = activelyRecruiting; }
}
