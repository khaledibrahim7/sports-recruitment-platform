package com.sports.web.controller;

import com.sports.auth.domain.Role;
import com.sports.auth.domain.User;
import com.sports.auth.repository.UserRepository;
import com.sports.academy.repository.AcademyProfileRepository;
import com.sports.club.repository.ClubProfileRepository;
import com.sports.common.dto.ApiResponse;
import com.sports.common.security.UserPrincipal;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/organizations")
@PreAuthorize("hasRole('ADMIN')")
public class OrganizationReviewController {
    private final UserRepository users;
    private final AcademyProfileRepository academies;
    private final ClubProfileRepository clubs;

    public OrganizationReviewController(UserRepository users, AcademyProfileRepository academies, ClubProfileRepository clubs) {
        this.users = users;
        this.academies = academies;
        this.clubs = clubs;
    }

    public record PendingOrganization(Long userId, String contactName, String email, Role role,
                                      String organizationName, String cityCountry, String phone, String website,
                                      boolean emailVerified) {}

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<PendingOrganization>>> pending() {
        List<PendingOrganization> pending = users.findByRoleInAndOrganizationApprovedFalseOrderByIdAsc(List.of(Role.ACADEMY, Role.CLUB)).stream()
                .map(u -> {
                    if (u.getRole() == Role.ACADEMY) {
                        return academies.findByUserId(u.getId()).map(p -> new PendingOrganization(
                                u.getId(), u.getFullName(), u.getEmail(), u.getRole(), p.getAcademyName(),
                                p.getCityCountry(), p.getContactPhone(), p.getWebsiteOrPageUrl(), u.isEmailVerified())).orElse(null);
                    }
                    return clubs.findByUserId(u.getId()).map(p -> new PendingOrganization(
                            u.getId(), u.getFullName(), u.getEmail(), u.getRole(), p.getClubName(),
                            p.getCityCountry(), p.getContactPhone(), p.getWebsiteOrPageUrl(), u.isEmailVerified())).orElse(null);
                })
                .filter(java.util.Objects::nonNull)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok("Pending organization accounts", pending));
    }

    @PostMapping("/{userId}/approve")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> approve(@PathVariable Long userId, Authentication authentication) {
        User user = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("Organization account not found"));
        if (user.getRole() != Role.ACADEMY && user.getRole() != Role.CLUB) {
            throw new IllegalArgumentException("Only academy and club accounts can be approved here");
        }
        if (!user.isEmailVerified()) {
            throw new IllegalArgumentException("The organization's email must be verified before review approval");
        }
        UserPrincipal reviewer = (UserPrincipal) authentication.getPrincipal();
        user.setOrganizationApproved(true);
        user.setOrganizationApprovedAt(Instant.now());
        user.setOrganizationApprovedBy(reviewer.getId());
        return ResponseEntity.ok(ApiResponse.ok("Organization account approved", null));
    }
}
