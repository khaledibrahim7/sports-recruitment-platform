package com.sports.auth.dto;

import com.sports.auth.domain.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 100, message = "Full name must be between 3 and 100 characters")
    private String fullName;

    @Valid
    private PlayerRegistration playerProfile;

    @Valid
    private AcademyRegistration academyProfile;

    @Valid
    private ClubRegistration clubProfile;

    // Frontend registration sends player profile fields at the request root for a flat API payload.
    private java.time.LocalDate birthDate;
    private String nationality;
    private String cityCountry;
    private String phone;
    private String position;
    private java.util.List<String> secondaryPositions = new java.util.ArrayList<>();
    @jakarta.validation.constraints.Pattern(regexp = "RIGHT|LEFT|BOTH", message = "Preferred foot must be RIGHT, LEFT, or BOTH")
    private String preferredFoot;
    private Integer heightCm;
    private Integer weightKg;
    private String currentTeam;
    @jakarta.validation.constraints.Pattern(regexp = "^$|AMATEUR|ACADEMY|SEMI_PRO|PROFESSIONAL", message = "Invalid competition level")
    private String competitionLevel;
    private String playerGoal;
    private Boolean availableForTransfer;
    private String videoUrl;
    private java.util.List<String> videoUrls = new java.util.ArrayList<>();
    private String bio;
    private boolean termsAccepted;

    @NotNull(message = "Role selection is required (PLAYER, ACADEMY, or CLUB)")
    private Role role;

    public RegisterRequest() {}

    public RegisterRequest(String email, String password, String fullName, Role role) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public PlayerRegistration getPlayerProfile() {
        if (playerProfile != null) return playerProfile;
        PlayerRegistration details = new PlayerRegistration();
        details.setBirthDate(birthDate);
        details.setNationality(nationality);
        details.setCityCountry(cityCountry);
        details.setPhone(phone);
        details.setPosition(position);
        details.setSecondaryPositions(secondaryPositions);
        details.setPreferredFoot(preferredFoot);
        details.setHeightCm(heightCm);
        details.setWeightKg(weightKg);
        details.setCurrentTeam(currentTeam);
        details.setCompetitionLevel(competitionLevel);
        details.setPlayerGoal(playerGoal);
        details.setAvailableForTransfer(availableForTransfer);
        details.setVideoUrl(videoUrl != null ? videoUrl : (videoUrls != null && !videoUrls.isEmpty() ? videoUrls.get(0) : null));
        details.setBio(bio);
        return details;
    }
    public PlayerRegistration getPlayerProfileForValidation() {
        return playerProfile != null ? playerProfile : (role == Role.PLAYER ? getPlayerProfile() : null);
    }
    public void setPlayerProfile(PlayerRegistration playerProfile) { this.playerProfile = playerProfile; }
    public AcademyRegistration getAcademyProfile() { return academyProfile; }
    public void setAcademyProfile(AcademyRegistration academyProfile) { this.academyProfile = academyProfile; }
    public ClubRegistration getClubProfile() { return clubProfile; }
    public void setClubProfile(ClubRegistration clubProfile) { this.clubProfile = clubProfile; }
    public java.time.LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(java.time.LocalDate birthDate) { this.birthDate = birthDate; }
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    public String getCityCountry() { return cityCountry; }
    public void setCityCountry(String cityCountry) { this.cityCountry = cityCountry; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public java.util.List<String> getSecondaryPositions() { return secondaryPositions; }
    public void setSecondaryPositions(java.util.List<String> secondaryPositions) { this.secondaryPositions = secondaryPositions; }
    public String getPreferredFoot() { return preferredFoot; }
    public void setPreferredFoot(String preferredFoot) { this.preferredFoot = preferredFoot; }
    public Integer getHeightCm() { return heightCm; }
    public void setHeightCm(Integer heightCm) { this.heightCm = heightCm; }
    public Integer getWeightKg() { return weightKg; }
    public void setWeightKg(Integer weightKg) { this.weightKg = weightKg; }
    public String getCurrentTeam() { return currentTeam; }
    public void setCurrentTeam(String currentTeam) { this.currentTeam = currentTeam; }
    public String getCompetitionLevel() { return competitionLevel; }
    public void setCompetitionLevel(String competitionLevel) { this.competitionLevel = competitionLevel; }
    public String getPlayerGoal() { return playerGoal; }
    public void setPlayerGoal(String playerGoal) { this.playerGoal = playerGoal; }
    public Boolean getAvailableForTransfer() { return availableForTransfer; }
    public void setAvailableForTransfer(Boolean availableForTransfer) { this.availableForTransfer = availableForTransfer; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public java.util.List<String> getVideoUrls() { return videoUrls; }
    public void setVideoUrls(java.util.List<String> videoUrls) { this.videoUrls = videoUrls; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    @jakarta.validation.constraints.AssertTrue(message = "Terms and privacy consent is required")
    public boolean isTermsAccepted() { return termsAccepted; }
    public void setTermsAccepted(boolean termsAccepted) { this.termsAccepted = termsAccepted; }

    public static class PlayerRegistration {
        @NotNull(message = "Birth date is required")
        @jakarta.validation.constraints.Past
        private java.time.LocalDate birthDate;

        @NotBlank(message = "Nationality is required")
        @Size(max = 80)
        private String nationality;

        @NotBlank(message = "City and country are required")
        @Size(max = 120)
        private String cityCountry;

        @NotBlank(message = "Phone is required")
        @Size(min = 7, max = 20)
        private String phone;

        @NotNull(message = "Player position is required")
        @NotBlank(message = "Player position is required")
        private String position;

        @Size(max = 1, message = "Only one secondary position may be selected at registration")
        private java.util.List<String> secondaryPositions = new java.util.ArrayList<>();

        @NotBlank(message = "Preferred foot is required")
        @jakarta.validation.constraints.Pattern(regexp = "RIGHT|LEFT|BOTH", message = "Preferred foot must be RIGHT, LEFT, or BOTH")
        private String preferredFoot;

        @jakarta.validation.constraints.Min(100) @jakarta.validation.constraints.Max(230)
        private Integer heightCm;
        @jakarta.validation.constraints.Min(30) @jakarta.validation.constraints.Max(200)
        private Integer weightKg;
        @Size(max = 120)
        private String currentTeam;
        @jakarta.validation.constraints.Pattern(regexp = "^$|AMATEUR|ACADEMY|SEMI_PRO|PROFESSIONAL", message = "Invalid competition level")
        private String competitionLevel;
        @Size(max = 500)
        private String playerGoal;
        private Boolean availableForTransfer;
        @Size(max = 500)
        private String videoUrl;
        @Size(max = 1000)
        private String bio;
        @Size(max = 120)
        private String guardianName;
        @Size(min = 7, max = 20)
        private String guardianPhone;
        private Boolean guardianConsent;

        public java.time.LocalDate getBirthDate() { return birthDate; }
        public void setBirthDate(java.time.LocalDate birthDate) { this.birthDate = birthDate; }
        public String getNationality() { return nationality; }
        public void setNationality(String nationality) { this.nationality = nationality; }
        public String getCityCountry() { return cityCountry; }
        public void setCityCountry(String cityCountry) { this.cityCountry = cityCountry; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getPosition() { return position; }
        public void setPosition(String position) { this.position = position; }
        public java.util.List<String> getSecondaryPositions() { return secondaryPositions; }
        public void setSecondaryPositions(java.util.List<String> secondaryPositions) { this.secondaryPositions = secondaryPositions; }
        public String getPreferredFoot() { return preferredFoot; }
        public void setPreferredFoot(String preferredFoot) { this.preferredFoot = preferredFoot; }
        public Integer getHeightCm() { return heightCm; }
        public void setHeightCm(Integer heightCm) { this.heightCm = heightCm; }
        public Integer getWeightKg() { return weightKg; }
        public void setWeightKg(Integer weightKg) { this.weightKg = weightKg; }
        public String getCurrentTeam() { return currentTeam; }
        public void setCurrentTeam(String currentTeam) { this.currentTeam = currentTeam; }
        public String getCompetitionLevel() { return competitionLevel; }
        public void setCompetitionLevel(String competitionLevel) { this.competitionLevel = competitionLevel; }
        public String getPlayerGoal() { return playerGoal; }
        public void setPlayerGoal(String playerGoal) { this.playerGoal = playerGoal; }
        public Boolean getAvailableForTransfer() { return availableForTransfer; }
        public void setAvailableForTransfer(Boolean availableForTransfer) { this.availableForTransfer = availableForTransfer; }
        public String getVideoUrl() { return videoUrl; }
        public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
        public String getBio() { return bio; }
        public void setBio(String bio) { this.bio = bio; }
        public String getGuardianName() { return guardianName; }
        public void setGuardianName(String guardianName) { this.guardianName = guardianName; }
        public String getGuardianPhone() { return guardianPhone; }
        public void setGuardianPhone(String guardianPhone) { this.guardianPhone = guardianPhone; }
        public Boolean getGuardianConsent() { return guardianConsent; }
        public void setGuardianConsent(Boolean guardianConsent) { this.guardianConsent = guardianConsent; }
    }

    public static class AcademyRegistration {
        @NotBlank(message = "Academy name is required") @Size(min = 2, max = 150)
        private String academyName;
        @NotBlank(message = "Academy city and country are required") @Size(max = 120)
        private String cityCountry;
        @NotBlank(message = "Academy contact phone is required") @Size(min = 7, max = 20)
        private String phone;
        @Size(max = 500)
        private String websiteOrPageUrl;
        @Size(max = 2000)
        private String description;
        private java.util.List<String> ageGroups = new java.util.ArrayList<>();
        @Size(max = 80)
        private String trainingType;
        public String getAcademyName() { return academyName; }
        public void setAcademyName(String academyName) { this.academyName = academyName; }
        public String getCityCountry() { return cityCountry; }
        public void setCityCountry(String cityCountry) { this.cityCountry = cityCountry; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getWebsiteOrPageUrl() { return websiteOrPageUrl; }
        public void setWebsiteOrPageUrl(String websiteOrPageUrl) { this.websiteOrPageUrl = websiteOrPageUrl; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public java.util.List<String> getAgeGroups() { return ageGroups; }
        public void setAgeGroups(java.util.List<String> ageGroups) { this.ageGroups = ageGroups; }
        public String getTrainingType() { return trainingType; }
        public void setTrainingType(String trainingType) { this.trainingType = trainingType; }
    }

    public static class ClubRegistration {
        @NotBlank(message = "Club name is required") @Size(min = 2, max = 150)
        private String clubName;
        @NotBlank(message = "Club city and country are required") @Size(max = 120)
        private String cityCountry;
        @NotBlank(message = "Club contact phone is required") @Size(min = 7, max = 20)
        private String phone;
        @Size(max = 500)
        private String websiteOrPageUrl;
        @Size(max = 2000)
        private String description;
        @Size(max = 80)
        private String leagueLevel;
        private Boolean activelyRecruiting;
        public String getClubName() { return clubName; }
        public void setClubName(String clubName) { this.clubName = clubName; }
        public String getCityCountry() { return cityCountry; }
        public void setCityCountry(String cityCountry) { this.cityCountry = cityCountry; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getWebsiteOrPageUrl() { return websiteOrPageUrl; }
        public void setWebsiteOrPageUrl(String websiteOrPageUrl) { this.websiteOrPageUrl = websiteOrPageUrl; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getLeagueLevel() { return leagueLevel; }
        public void setLeagueLevel(String leagueLevel) { this.leagueLevel = leagueLevel; }
        public Boolean getActivelyRecruiting() { return activelyRecruiting; }
        public void setActivelyRecruiting(Boolean activelyRecruiting) { this.activelyRecruiting = activelyRecruiting; }
    }
}
