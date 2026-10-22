package com.sports.academy.service;

import com.sports.academy.domain.AcademyProfile;
import com.sports.academy.dto.AcademyProfileDto;
import com.sports.academy.dto.AcademyProfileUpdateRequest;
import com.sports.academy.repository.AcademyProfileRepository;
import com.sports.auth.domain.User;
import com.sports.auth.repository.UserRepository;
import com.sports.common.exception.EntityNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AcademyProfileServiceImpl implements AcademyProfileService {

    private final AcademyProfileRepository academyProfileRepository;
    private final UserRepository userRepository;

    public AcademyProfileServiceImpl(AcademyProfileRepository academyProfileRepository, UserRepository userRepository) {
        this.academyProfileRepository = academyProfileRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "academies")
    public List<AcademyProfileDto> getAllAcademies() {
        return academyProfileRepository.findAll().stream()
                .map(AcademyProfileDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AcademyProfileDto.PublicProfile> getPublicAcademies() {
        return academyProfileRepository.findAll().stream().map(AcademyProfileDto.PublicProfile::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AcademyProfileDto.PublicProfile getPublicAcademyById(Long id) {
        AcademyProfile profile = academyProfileRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("AcademyProfile", id.toString()));
        return AcademyProfileDto.PublicProfile.fromEntity(profile);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "academyById", key = "#id")
    public AcademyProfileDto getAcademyById(Long id) {
        AcademyProfile profile = academyProfileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("AcademyProfile", id.toString()));
        return AcademyProfileDto.fromEntity(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public AcademyProfileDto getAcademyByUserId(Long userId) {
        AcademyProfile profile = academyProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("AcademyProfile for User", userId.toString()));
        return AcademyProfileDto.fromEntity(profile);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"academies", "academyById"}, allEntries = true)
    public AcademyProfileDto createOrUpdateProfile(Long userId, AcademyProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId.toString()));

        AcademyProfile profile = academyProfileRepository.findByUserId(userId)
                .orElse(new AcademyProfile());

        profile.setUser(user);
        profile.setAcademyName(request.getAcademyName());
        profile.setWebsiteOrPageUrl(request.getWebsiteOrPageUrl());
        profile.setCityCountry(request.getCityCountry());
        profile.setContactPhone(request.getContactPhone());
        profile.setDescription(request.getDescription());
        profile.setLogoUrl(request.getLogoUrl());
        if (request.getAgeGroups() != null) profile.setAgeGroups(request.getAgeGroups());
        if (request.getTrainingType() != null) profile.setTrainingType(request.getTrainingType());

        AcademyProfile saved = academyProfileRepository.save(profile);
        return AcademyProfileDto.fromEntity(saved);
    }
}
