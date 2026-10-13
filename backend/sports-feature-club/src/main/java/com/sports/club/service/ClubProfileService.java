package com.sports.club.service;

import com.sports.club.dto.ClubProfileDto;
import com.sports.club.dto.ClubProfileUpdateRequest;

import java.util.List;

public interface ClubProfileService {
    List<ClubProfileDto> getAllClubs();
    ClubProfileDto getClubById(Long id);
    ClubProfileDto getClubByUserId(Long userId);
    ClubProfileDto createOrUpdateProfile(Long userId, ClubProfileUpdateRequest request);
}
