package com.sports.club.service;

import com.sports.club.dto.ClubProfileDto;
import com.sports.club.dto.ClubProfileUpdateRequest;

import java.util.List;

public interface ClubProfileService {
    List<ClubProfileDto> getAllClubs();
    List<ClubProfileDto.PublicProfile> getPublicClubs();
    ClubProfileDto getClubById(Long id);
    ClubProfileDto.PublicProfile getPublicClubById(Long id);
    ClubProfileDto getClubByUserId(Long userId);
    ClubProfileDto createOrUpdateProfile(Long userId, ClubProfileUpdateRequest request);
}
