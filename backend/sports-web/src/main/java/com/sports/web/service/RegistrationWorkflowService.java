package com.sports.web.service;

import com.sports.academy.domain.AcademyProfile;
import com.sports.academy.repository.AcademyProfileRepository;
import com.sports.auth.domain.Role;
import com.sports.auth.domain.User;
import com.sports.auth.dto.AuthResponse;
import com.sports.auth.dto.RegisterRequest;
import com.sports.auth.repository.UserRepository;
import com.sports.auth.service.AuthService;
import com.sports.club.domain.ClubProfile;
import com.sports.club.repository.ClubProfileRepository;
import com.sports.player.domain.PlayerPosition;
import com.sports.player.domain.PlayerProfile;
import com.sports.player.dto.PlayerProfileDto;
import com.sports.player.repository.PlayerProfileRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RegistrationWorkflowService {
    private final AuthService authService;
    private final PlayerProfileRepository playerProfiles;
    private final AcademyProfileRepository academyProfiles;
    private final ClubProfileRepository clubProfiles;
    private final UserRepository users;
    private final Validator validator;
    private final AccountVerificationService verificationService;

    public RegistrationWorkflowService(AuthService authService, PlayerProfileRepository playerProfiles,
                                      AcademyProfileRepository academyProfiles, ClubProfileRepository clubProfiles,
                                      UserRepository users, Validator validator,
                                      AccountVerificationService verificationService) {
        this.authService = authService;
        this.playerProfiles = playerProfiles;
        this.academyProfiles = academyProfiles;
        this.clubProfiles = clubProfiles;
        this.users = users;
        this.validator = validator;
        this.verificationService = verificationService;
    }

    @Transactional
    public void register(RegisterRequest request) {
        validateRoleAndOrganizationDetails(request);
        AuthResponse response = authService.register(request);
        if (response.getUserId() == null) return;

        String guardianEmail = switch (request.getRole()) {
            case PLAYER -> createPlayerProfile(request, response);
            case ACADEMY -> { createAcademyProfile(request, response); yield null; }
            case CLUB -> { createClubProfile(request, response); yield null; }
            default -> throw new IllegalArgumentException("Choose a supported account type");
        };
        User registeredUser = users.findById(response.getUserId()).orElseThrow();
        verificationService.sendRegistrationEmails(registeredUser, guardianEmail);
    }

    private void validateRoleAndOrganizationDetails(RegisterRequest request) {
        if (request.getRole() == null || request.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Choose a supported account type");
        }
        if (request.getRole() == Role.ACADEMY && request.getAcademyProfile() == null) {
            throw new IllegalArgumentException("Academy registration details are required");
        }
        if (request.getRole() == Role.CLUB && request.getClubProfile() == null) {
            throw new IllegalArgumentException("Club registration details are required");
        }
    }

    private String createPlayerProfile(RegisterRequest request, AuthResponse response) {
        RegisterRequest.PlayerRegistration details = request.getPlayerProfileForValidation();
        if (details == null) {
            throw new IllegalArgumentException("Required player profile information is missing");
        }
        Set<ConstraintViolation<RegisterRequest.PlayerRegistration>> violations = validator.validate(details);
        if (!violations.isEmpty()) throw new ConstraintViolationException(violations);
        if (details.getBirthDate() == null || details.getNationality() == null || details.getCityCountry() == null
                || details.getPhone() == null || details.getPosition() == null || details.getPreferredFoot() == null) {
            throw new IllegalArgumentException("Required player profile information is missing");
        }
        LocalDate birthDate = details.getBirthDate();
        LocalDate today = LocalDate.now();
        if (birthDate.isAfter(today) || birthDate.isBefore(today.minusYears(60))) {
            throw new IllegalArgumentException("Birth date must be within the last 60 years and cannot be in the future");
        }
        int age = Period.between(birthDate, today).getYears();
        boolean minor = age < 18;
        if (minor && (blank(details.getGuardianName()) || blank(details.getGuardianPhone())
                || !Boolean.TRUE.equals(details.getGuardianConsent()) || blank(details.getGuardianEmail()))) {
            throw new IllegalArgumentException("Guardian name, phone, email, and consent are required for players under 18");
        }
        if (minor && details.getGuardianEmail().trim().equalsIgnoreCase(request.getEmail())) {
            throw new IllegalArgumentException("A guardian email different from the player's email is required");
        }

        PlayerPosition primaryPosition = parsePosition(details.getPosition());
        PlayerProfile profile = new PlayerProfile();
        profile.setUserId(response.getUserId());
        profile.setFullName(response.getFullName());
        profile.setAge(age);
        profile.setPosition(primaryPosition);
        profile.setBirthDate(birthDate);
        profile.setNationality(details.getNationality().trim());
        profile.setCityCountry(details.getCityCountry().trim());
        profile.setPhone(details.getPhone().trim());
        profile.setPreferredFoot(details.getPreferredFoot());
        profile.setHeightCm(details.getHeightCm());
        profile.setWeightKg(details.getWeightKg());
        profile.setCurrentTeam(trimToNull(details.getCurrentTeam()));
        profile.setCompetitionLevel(details.getCompetitionLevel());
        profile.setPlayerGoal(details.getPlayerGoal());
        profile.setAvailableForTransfer(Boolean.TRUE.equals(details.getAvailableForTransfer()));
        if (minor) {
            profile.setGuardianName(details.getGuardianName().trim());
            profile.setGuardianPhone(details.getGuardianPhone().trim());
        }
        profile.setVideoUrl(details.getVideoUrl());
        profile.setBio(trimToNull(details.getBio()));
        if (details.getSecondaryPositions() != null && !details.getSecondaryPositions().isEmpty()) {
            Set<PlayerPosition> secondary = details.getSecondaryPositions().stream().map(this::parsePosition).collect(Collectors.toSet());
            secondary.remove(primaryPosition);
            if (!secondary.isEmpty()) profile.setSecondaryPositions(secondary);
        }
        if (!blank(details.getVideoUrl())) profile.setVideoUrls(List.of(details.getVideoUrl()));
        playerProfiles.save(profile);
        return minor ? details.getGuardianEmail() : null;
    }

    private void createAcademyProfile(RegisterRequest request, AuthResponse response) {
        var details = request.getAcademyProfile();
        User user = users.findById(response.getUserId()).orElseThrow();
        AcademyProfile profile = new AcademyProfile();
        profile.setUser(user);
        profile.setAcademyName(details.getAcademyName().trim());
        profile.setCityCountry(details.getCityCountry().trim());
        profile.setContactPhone(details.getPhone().trim());
        profile.setWebsiteOrPageUrl(details.getWebsiteOrPageUrl());
        profile.setDescription(details.getDescription());
        profile.setAgeGroups(details.getAgeGroups());
        profile.setTrainingType(details.getTrainingType());
        academyProfiles.save(profile);
    }

    private void createClubProfile(RegisterRequest request, AuthResponse response) {
        var details = request.getClubProfile();
        User user = users.findById(response.getUserId()).orElseThrow();
        ClubProfile profile = new ClubProfile();
        profile.setUser(user);
        profile.setClubName(details.getClubName().trim());
        profile.setCityCountry(details.getCityCountry().trim());
        profile.setContactPhone(details.getPhone().trim());
        profile.setWebsiteOrPageUrl(details.getWebsiteOrPageUrl());
        profile.setDescription(details.getDescription());
        profile.setLeagueLevel(details.getLeagueLevel());
        profile.setActivelyRecruiting(Boolean.TRUE.equals(details.getActivelyRecruiting()));
        clubProfiles.save(profile);
    }

    private PlayerPosition parsePosition(String position) {
        try { return PlayerPosition.valueOf(position); }
        catch (RuntimeException ex) { throw new IllegalArgumentException("Invalid player position"); }
    }

    private static boolean blank(String value) { return value == null || value.isBlank(); }
    private static String trimToNull(String value) { return blank(value) ? null : value.trim(); }
}
