package com.sports.web.controller;

import com.sports.academy.dto.AcademyProfileDto;
import com.sports.academy.dto.AcademyProfileUpdateRequest;
import com.sports.academy.service.AcademyProfileService;
import com.sports.common.security.UserPrincipal;
import com.sports.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/academies")
@Tag(name = "Academies Directory", description = "Academy Profiles Endpoints")
public class AcademyProfileController {

    private final AcademyProfileService academyProfileService;

    public AcademyProfileController(AcademyProfileService academyProfileService) {
        this.academyProfileService = academyProfileService;
    }

    @GetMapping
    @Operation(summary = "Get list of all registered academies")
    public ResponseEntity<ApiResponse<List<AcademyProfileDto.PublicProfile>>> getAllAcademies() {
        List<AcademyProfileDto.PublicProfile> academies = academyProfileService.getPublicAcademies();
        return ResponseEntity.ok(ApiResponse.ok(academies));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get academy profile by ID")
    public ResponseEntity<ApiResponse<AcademyProfileDto.PublicProfile>> getAcademyById(@PathVariable Long id) {
        AcademyProfileDto.PublicProfile academy = academyProfileService.getPublicAcademyById(id);
        return ResponseEntity.ok(ApiResponse.ok(academy));
    }

    @PutMapping("/me")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ACADEMY')")
    @Operation(summary = "Update authenticated academy's profile")
    public ResponseEntity<ApiResponse<AcademyProfileDto>> updateMyProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody AcademyProfileUpdateRequest request) {
        AcademyProfileDto updated = academyProfileService.createOrUpdateProfile(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Academy profile updated successfully", updated));
    }

    @GetMapping("/me")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ACADEMY')")
    public ResponseEntity<ApiResponse<AcademyProfileDto>> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(academyProfileService.getAcademyByUserId(principal.getId())));
    }
}
