package com.sports.web.controller;

import com.sports.common.dto.ApiResponse;
import com.sports.common.security.UserPrincipal;
import com.sports.web.service.OrganizationReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/organizations")
@PreAuthorize("hasRole('ADMIN')")
public class OrganizationReviewController {
    private final OrganizationReviewService reviewService;

    public OrganizationReviewController(OrganizationReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<OrganizationReviewService.PendingOrganization>>> pending() {
        return ResponseEntity.ok(ApiResponse.ok("Pending organization accounts", reviewService.pending()));
    }

    @PostMapping("/{userId}/approve")
    public ResponseEntity<ApiResponse<Void>> approve(@PathVariable Long userId, Authentication authentication) {
        UserPrincipal reviewer = (UserPrincipal) authentication.getPrincipal();
        reviewService.approve(userId, reviewer.getId());
        return ResponseEntity.ok(ApiResponse.ok("Organization account approved", null));
    }
}
