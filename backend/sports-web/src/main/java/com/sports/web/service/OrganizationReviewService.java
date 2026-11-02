package com.sports.web.service;

import com.sports.academy.domain.AcademyProfile;
import com.sports.academy.repository.AcademyProfileRepository;
import com.sports.auth.domain.Role;
import com.sports.auth.domain.User;
import com.sports.auth.repository.UserRepository;
import com.sports.club.domain.ClubProfile;
import com.sports.club.repository.ClubProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrganizationReviewService {
    private final UserRepository users;
    private final AcademyProfileRepository academies;
    private final ClubProfileRepository clubs;

    public OrganizationReviewService(UserRepository users, AcademyProfileRepository academies, ClubProfileRepository clubs) {
        this.users = users;
        this.academies = academies;
        this.clubs = clubs;
    }

    @Transactional(readOnly = true)
    public List<PendingOrganization> pending() {
        List<User> pendingUsers = users.findByRoleInAndOrganizationApprovedFalseOrderByIdAsc(List.of(Role.ACADEMY, Role.CLUB));
        if (pendingUsers.isEmpty()) return List.of();
        List<Long> ids = pendingUsers.stream().map(User::getId).toList();
        Map<Long, AcademyProfile> academyByUser = academies.findByUser_IdIn(ids).stream()
                .collect(Collectors.toMap(profile -> profile.getUser().getId(), Function.identity()));
        Map<Long, ClubProfile> clubByUser = clubs.findByUser_IdIn(ids).stream()
                .collect(Collectors.toMap(profile -> profile.getUser().getId(), Function.identity()));

        return pendingUsers.stream().map(user -> {
            if (user.getRole() == Role.ACADEMY) {
                AcademyProfile profile = academyByUser.get(user.getId());
                return profile == null ? null : new PendingOrganization(user.getId(), user.getFullName(), user.getEmail(),
                        user.getRole(), profile.getAcademyName(), profile.getCityCountry(), profile.getContactPhone(),
                        profile.getWebsiteOrPageUrl(), user.isEmailVerified());
            }
            ClubProfile profile = clubByUser.get(user.getId());
            return profile == null ? null : new PendingOrganization(user.getId(), user.getFullName(), user.getEmail(),
                    user.getRole(), profile.getClubName(), profile.getCityCountry(), profile.getContactPhone(),
                    profile.getWebsiteOrPageUrl(), user.isEmailVerified());
        }).filter(java.util.Objects::nonNull).toList();
    }

    @Transactional
    public void approve(Long userId, Long reviewerId) {
        User user = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("Organization account not found"));
        if (user.getRole() != Role.ACADEMY && user.getRole() != Role.CLUB) {
            throw new IllegalArgumentException("Only academy and club accounts can be approved here");
        }
        if (!user.isEmailVerified()) {
            throw new IllegalArgumentException("The organization's email must be verified before review approval");
        }
        if (user.isOrganizationApproved()) return;
        user.setOrganizationApproved(true);
        user.setOrganizationApprovedAt(Instant.now());
        user.setOrganizationApprovedBy(reviewerId);
    }

    public record PendingOrganization(Long userId, String contactName, String email, Role role,
                                      String organizationName, String cityCountry, String phone, String website,
                                      boolean emailVerified) {}
}
