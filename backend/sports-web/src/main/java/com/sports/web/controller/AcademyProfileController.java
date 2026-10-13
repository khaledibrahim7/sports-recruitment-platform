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
    public ResponseEntity<ApiResponse<List<AcademyProfileDto>>> getAllAcademies() {
        List<AcademyProfileDto> academies = academyProfileService.getAllAcademies();
        return ResponseEntity.ok(ApiResponse.ok(academies));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get academy profile by ID")
    public ResponseEntity<ApiResponse<AcademyProfileDto>> getAcademyById(@PathVariable Long id) {
        AcademyProfileDto academy = academyProfileService.getAcademyById(id);
        return ResponseEntity.ok(ApiResponse.ok(academy));
    }

    @PutMapping("/me")
    @Operation(summary = "Update authenticated academy's profile")
    public ResponseEntity<ApiResponse<AcademyProfileDto>> updateMyProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody AcademyProfileUpdateRequest request) {
        AcademyProfileDto updated = academyProfileService.createOrUpdateProfile(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Academy profile updated successfully", updated));
    }
}
