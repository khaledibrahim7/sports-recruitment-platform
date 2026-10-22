package com.sports.web.controller;

import com.sports.auth.dto.AuthResponse;
import com.sports.auth.dto.LoginRequest;
import com.sports.auth.dto.RegisterRequest;
import com.sports.auth.service.AuthService;
import com.sports.player.domain.PlayerProfile;
import com.sports.player.dto.PlayerProfileDto;
import com.sports.player.repository.PlayerProfileRepository;
import com.sports.academy.domain.AcademyProfile;
import com.sports.academy.dto.AcademyProfileDto;
import com.sports.academy.repository.AcademyProfileRepository;
import com.sports.club.domain.ClubProfile;
import com.sports.club.dto.ClubProfileDto;
import com.sports.club.repository.ClubProfileRepository;
import com.sports.auth.domain.User;
import com.sports.auth.repository.UserRepository;
import com.sports.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication & Accounts", description = "Frictionless Authentication & Registration Endpoints")
public class AuthController {

    private final AuthService authService;
    private final PlayerProfileRepository playerProfileRepository;
    private final AcademyProfileRepository academyProfileRepository;
    private final ClubProfileRepository clubProfileRepository;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, PlayerProfileRepository playerProfileRepository,
                          AcademyProfileRepository academyProfileRepository, ClubProfileRepository clubProfileRepository,
                          UserRepository userRepository) {
        this.authService = authService;
        this.playerProfileRepository = playerProfileRepository;
        this.academyProfileRepository = academyProfileRepository;
        this.clubProfileRepository = clubProfileRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    @Transactional
    @Operation(summary = "Frictionless registration for new user account with selected role")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        if (request.getRole() == com.sports.auth.domain.Role.ACADEMY && request.getAcademyProfile() == null) {
            throw new IllegalArgumentException("Academy registration details are required");
        }
        if (request.getRole() == com.sports.auth.domain.Role.CLUB && request.getClubProfile() == null) {
            throw new IllegalArgumentException("Club registration details are required");
        }
        AuthResponse authResponse = authService.register(request);
        if (request.getRole() == com.sports.auth.domain.Role.PLAYER) {
            RegisterRequest.PlayerRegistration details = request.getPlayerProfileForValidation();
            if (details == null || details.getBirthDate() == null || details.getNationality() == null || details.getCityCountry() == null
                    || details.getPhone() == null || details.getPosition() == null || details.getPreferredFoot() == null) {
                throw new IllegalArgumentException("Required player profile information is missing");
            }
            java.time.LocalDate birthDate = details.getBirthDate();
            java.time.LocalDate today = java.time.LocalDate.now();
            if (birthDate.isAfter(today) || birthDate.isBefore(today.minusYears(60))) {
                throw new IllegalArgumentException("Birth date must be within the last 60 years and cannot be in the future");
            }
            int age = java.time.Period.between(birthDate, today).getYears();
            if (age < 18 && (details.getGuardianName() == null || details.getGuardianName().isBlank()
                    || details.getGuardianPhone() == null || details.getGuardianPhone().isBlank()
                    || !Boolean.TRUE.equals(details.getGuardianConsent()))) {
                throw new IllegalArgumentException("Guardian name, phone, and consent are required for players under 18");
            }
            PlayerProfile profile = new PlayerProfile();
            profile.setUserId(authResponse.getUserId());
            profile.setUser(null);
            profile.setFullName(authResponse.getFullName());
            profile.setAge(age);
            com.sports.player.domain.PlayerPosition primaryPosition = parsePosition(details.getPosition());
            profile.setPosition(primaryPosition);
            profile.setBirthDate(birthDate);
            profile.setNationality(details.getNationality().trim());
            profile.setCityCountry(details.getCityCountry().trim());
            profile.setPhone(details.getPhone().trim());
            profile.setPreferredFoot(details.getPreferredFoot());
            profile.setHeightCm(details.getHeightCm());
            profile.setWeightKg(details.getWeightKg());
            profile.setCurrentTeam(details.getCurrentTeam() != null ? details.getCurrentTeam().trim() : null);
            profile.setCompetitionLevel(details.getCompetitionLevel());
            profile.setPlayerGoal(details.getPlayerGoal());
            profile.setAvailableForTransfer(Boolean.TRUE.equals(details.getAvailableForTransfer()));
            if (age < 18) {
                profile.setGuardianName(details.getGuardianName().trim());
                profile.setGuardianPhone(details.getGuardianPhone().trim());
                profile.setGuardianConsentAt(java.time.Instant.now());
            }
            profile.setVideoUrl(details.getVideoUrl());
            profile.setBio(details.getBio() != null ? details.getBio().trim() : null);
            if (details.getSecondaryPositions() != null) {
                java.util.Set<com.sports.player.domain.PlayerPosition> secondary = details.getSecondaryPositions().stream()
                        .map(this::parsePosition)
                        .collect(java.util.stream.Collectors.toSet());
                secondary.remove(primaryPosition);
                profile.setSecondaryPositions(secondary);
            }
            if (details.getVideoUrl() != null && !details.getVideoUrl().isBlank()) {
                profile.setVideoUrls(java.util.List.of(details.getVideoUrl()));
            }
            PlayerProfile savedProfile = playerProfileRepository.save(profile);
            authResponse.setProfile(PlayerProfileDto.fromEntity(savedProfile));
        } else if (request.getRole() == com.sports.auth.domain.Role.ACADEMY) {
            RegisterRequest.AcademyRegistration details = request.getAcademyProfile();
            User user = userRepository.findById(authResponse.getUserId()).orElseThrow();
            AcademyProfile profile = new AcademyProfile();
            profile.setUser(user);
            profile.setAcademyName(details.getAcademyName().trim());
            profile.setCityCountry(details.getCityCountry().trim());
            profile.setContactPhone(details.getPhone().trim());
            profile.setWebsiteOrPageUrl(details.getWebsiteOrPageUrl());
            profile.setDescription(details.getDescription());
            profile.setAgeGroups(details.getAgeGroups());
            profile.setTrainingType(details.getTrainingType());
            authResponse.setProfile(AcademyProfileDto.fromEntity(academyProfileRepository.save(profile)));
        } else if (request.getRole() == com.sports.auth.domain.Role.CLUB) {
            RegisterRequest.ClubRegistration details = request.getClubProfile();
            User user = userRepository.findById(authResponse.getUserId()).orElseThrow();
            ClubProfile profile = new ClubProfile();
            profile.setUser(user);
            profile.setClubName(details.getClubName().trim());
            profile.setCityCountry(details.getCityCountry().trim());
            profile.setContactPhone(details.getPhone().trim());
            profile.setWebsiteOrPageUrl(details.getWebsiteOrPageUrl());
            profile.setDescription(details.getDescription());
            profile.setLeagueLevel(details.getLeagueLevel());
            profile.setActivelyRecruiting(Boolean.TRUE.equals(details.getActivelyRecruiting()));
            authResponse.setProfile(ClubProfileDto.fromEntity(clubProfileRepository.save(profile)));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("Account registered successfully", authResponse));
    }

    private com.sports.player.domain.PlayerPosition parsePosition(String position) {
        try {
            return com.sports.player.domain.PlayerPosition.valueOf(position);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Invalid player position");
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user credentials and return JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", authResponse));
    }
}
