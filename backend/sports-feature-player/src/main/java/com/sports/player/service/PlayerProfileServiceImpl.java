package com.sports.player.service;

import com.sports.auth.domain.User;
import com.sports.auth.repository.UserRepository;
import com.sports.common.exception.EntityNotFoundException;
import com.sports.player.domain.PlayerPosition;
import com.sports.player.domain.PlayerProfile;
import com.sports.player.dto.PlayerProfileDto;
import com.sports.player.dto.PlayerProfileUpdateRequest;
import com.sports.player.repository.PlayerProfileRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlayerProfileServiceImpl implements PlayerProfileService {

    private final PlayerProfileRepository playerProfileRepository;
    private final UserRepository userRepository;

    public PlayerProfileServiceImpl(PlayerProfileRepository playerProfileRepository, UserRepository userRepository) {
        this.playerProfileRepository = playerProfileRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "players", key = "#position != null ? #position.name() + '-' + #age : (#age != null ? 'all-' + #age : 'all')")
    public List<PlayerProfileDto> getAllPlayers(PlayerPosition position, Integer age) {
        List<PlayerProfile> profiles;
        if (position != null && age != null) {
            profiles = playerProfileRepository.findByPositionAndAge(position, age);
        } else if (position != null) {
            profiles = playerProfileRepository.findByPosition(position);
        } else if (age != null) {
            profiles = playerProfileRepository.findByAge(age);
        } else {
            profiles = playerProfileRepository.findAll();
        }

        return profiles.stream()
                .map(PlayerProfileDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "playerById", key = "#id")
    public PlayerProfileDto getPlayerById(Long id) {
        PlayerProfile profile = playerProfileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PlayerProfile", id.toString()));
        return PlayerProfileDto.fromEntity(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public PlayerProfileDto getPlayerByUserId(Long userId) {
        PlayerProfile profile = playerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("PlayerProfile for User", userId.toString()));
        return PlayerProfileDto.fromEntity(profile);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"players", "playerById"}, allEntries = true)
    public PlayerProfileDto createOrUpdateProfile(Long userId, PlayerProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId.toString()));

        PlayerProfile profile = playerProfileRepository.findByUserId(userId)
                .orElse(new PlayerProfile());

        profile.setUser(user);
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            profile.setFullName(request.getFullName());
        } else if (profile.getFullName() == null) {
            profile.setFullName(user.getFullName());
        }

        if (request.getAge() != null) {
            profile.setAge(request.getAge());
        }
        if (request.getPosition() != null) {
            profile.setPosition(request.getPosition());
        }
        if (request.getSecondaryPositions() != null) {
            profile.setSecondaryPositions(new HashSet<>(request.getSecondaryPositions()));
        }
        if (request.getVideoUrl() != null && !request.getVideoUrl().isBlank()) {
            profile.setVideoUrl(request.getVideoUrl());
        }
        if (request.getVideoUrls() != null && !request.getVideoUrls().isEmpty()) {
            List<String> limitedUrls = request.getVideoUrls().stream()
                    .filter(u -> u != null && !u.isBlank())
                    .limit(3)
                    .collect(Collectors.toList());
            profile.setVideoUrls(limitedUrls);
            if (profile.getVideoUrl() == null || profile.getVideoUrl().isBlank()) {
                profile.setVideoUrl(limitedUrls.isEmpty() ? null : limitedUrls.get(0));
            }
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getPhone() != null) {
            profile.setPhone(request.getPhone());
        }
        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getHeightCm() != null) profile.setHeightCm(request.getHeightCm());
        if (request.getWeightKg() != null) profile.setWeightKg(request.getWeightKg());
        if (request.getPreferredFoot() != null) profile.setPreferredFoot(request.getPreferredFoot());
        if (request.getPace() != null) profile.setPace(request.getPace());
        if (request.getShooting() != null) profile.setShooting(request.getShooting());
        if (request.getPassing() != null) profile.setPassing(request.getPassing());
        if (request.getDribbling() != null) profile.setDribbling(request.getDribbling());
        if (request.getDefending() != null) profile.setDefending(request.getDefending());
        if (request.getPhysicality() != null) profile.setPhysicality(request.getPhysicality());

        PlayerProfile saved = playerProfileRepository.save(profile);
        return PlayerProfileDto.fromEntity(saved);
    }
}
