package com.sports.web.controller;

import com.sports.common.security.UserPrincipal;
import com.sports.club.dto.ClubProfileDto;
import com.sports.club.dto.ClubProfileUpdateRequest;
import com.sports.club.service.ClubProfileService;
import com.sports.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clubs")
@Tag(name = "Clubs Directory", description = "Club Profiles Endpoints")
public class ClubProfileController {

    private final ClubProfileService clubProfileService;

    public ClubProfileController(ClubProfileService clubProfileService) {
        this.clubProfileService = clubProfileService;
    }

    @GetMapping
    @Operation(summary = "Get list of all registered sports clubs")
    public ResponseEntity<ApiResponse<List<ClubProfileDto.PublicProfile>>> getAllClubs() {
        List<ClubProfileDto.PublicProfile> clubs = clubProfileService.getPublicClubs();
        return ResponseEntity.ok(ApiResponse.ok(clubs));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get club profile by ID")
    public ResponseEntity<ApiResponse<ClubProfileDto.PublicProfile>> getClubById(@PathVariable Long id) {
        ClubProfileDto.PublicProfile club = clubProfileService.getPublicClubById(id);
        return ResponseEntity.ok(ApiResponse.ok(club));
    }

    @PutMapping("/me")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('CLUB')")
    @Operation(summary = "Update authenticated club's profile")
    public ResponseEntity<ApiResponse<ClubProfileDto>> updateMyProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody ClubProfileUpdateRequest request) {
        ClubProfileDto updated = clubProfileService.createOrUpdateProfile(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Club profile updated successfully", updated));
    }

    @GetMapping("/me")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('CLUB')")
    public ResponseEntity<ApiResponse<ClubProfileDto>> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(clubProfileService.getClubByUserId(principal.getId())));
    }
}
