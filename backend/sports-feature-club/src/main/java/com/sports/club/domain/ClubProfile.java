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
}
