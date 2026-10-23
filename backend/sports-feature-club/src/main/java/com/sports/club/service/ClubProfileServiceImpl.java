package com.sports.club.service;

import com.sports.auth.domain.User;
import com.sports.auth.repository.UserRepository;
import com.sports.club.domain.ClubProfile;
import com.sports.club.dto.ClubProfileDto;
import com.sports.club.dto.ClubProfileUpdateRequest;
import com.sports.club.repository.ClubProfileRepository;
import com.sports.common.exception.EntityNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClubProfileServiceImpl implements ClubProfileService {

    private final ClubProfileRepository clubProfileRepository;
    private final UserRepository userRepository;

    public ClubProfileServiceImpl(ClubProfileRepository clubProfileRepository, UserRepository userRepository) {
        this.clubProfileRepository = clubProfileRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "clubs")
    public List<ClubProfileDto> getAllClubs() {
        return clubProfileRepository.findAll().stream()
                .map(ClubProfileDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClubProfileDto.PublicProfile> getPublicClubs() {
        return clubProfileRepository.findAll().stream()
                .filter(profile -> profile.getUser() != null && profile.getUser().isEmailVerified() && profile.getUser().isOrganizationApproved())
                .map(ClubProfileDto.PublicProfile::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ClubProfileDto.PublicProfile getPublicClubById(Long id) {
        ClubProfile profile = clubProfileRepository.findById(id)
                .filter(p -> p.getUser() != null && p.getUser().isEmailVerified() && p.getUser().isOrganizationApproved())
                .orElseThrow(() -> new EntityNotFoundException("ClubProfile", id.toString()));
        return ClubProfileDto.PublicProfile.fromEntity(profile);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "clubById", key = "#id")
    public ClubProfileDto getClubById(Long id) {
        ClubProfile profile = clubProfileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ClubProfile", id.toString()));
        return ClubProfileDto.fromEntity(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public ClubProfileDto getClubByUserId(Long userId) {
        ClubProfile profile = clubProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("ClubProfile for User", userId.toString()));
        return ClubProfileDto.fromEntity(profile);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"clubs", "clubById"}, allEntries = true)
    public ClubProfileDto createOrUpdateProfile(Long userId, ClubProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId.toString()));

        ClubProfile profile = clubProfileRepository.findByUserId(userId)
                .orElse(new ClubProfile());

        profile.setUser(user);
        profile.setClubName(request.getClubName());
        profile.setLogoUrl(request.getLogoUrl());
        profile.setCityCountry(request.getCityCountry());
        profile.setContactPhone(request.getContactPhone());
        profile.setWebsiteOrPageUrl(request.getWebsiteOrPageUrl());
        profile.setDescription(request.getDescription());
        profile.setLeagueLevel(request.getLeagueLevel());
        if (request.getActivelyRecruiting() != null) profile.setActivelyRecruiting(request.getActivelyRecruiting());

        ClubProfile saved = clubProfileRepository.save(profile);
        return ClubProfileDto.fromEntity(saved);
    }
}
