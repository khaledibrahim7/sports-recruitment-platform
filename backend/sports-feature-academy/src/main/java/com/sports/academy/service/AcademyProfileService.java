package com.sports.academy.service;

import com.sports.academy.dto.AcademyProfileDto;
import com.sports.academy.dto.AcademyProfileUpdateRequest;

import java.util.List;

public interface AcademyProfileService {
    List<AcademyProfileDto> getAllAcademies();
    AcademyProfileDto getAcademyById(Long id);
    AcademyProfileDto getAcademyByUserId(Long userId);
    AcademyProfileDto createOrUpdateProfile(Long userId, AcademyProfileUpdateRequest request);
}
