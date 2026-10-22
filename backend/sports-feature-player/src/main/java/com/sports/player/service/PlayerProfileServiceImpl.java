package com.sports.player.service;

import com.sports.auth.domain.User;
import com.sports.auth.repository.UserRepository;
import com.sports.common.exception.EntityNotFoundException;
import com.sports.player.domain.PlayerPosition;
import com.sports.player.domain.PlayerProfile;
import com.sports.player.dto.PlayerProfileDto;
import com.sports.player.dto.PublicPlayerProfileDto;
import com.sports.player.dto.PlayerProfileUpdateRequest;
import com.sports.player.repository.PlayerProfileRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
        return getMatchingProfiles(position, age).stream()
                .map(PlayerProfileDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicPlayerProfileDto> getPublicPlayers(PlayerPosition position, Integer age) {
        return getMatchingProfiles(position, age).stream().map(PublicPlayerProfileDto::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PublicPlayerProfileDto> searchPublicPlayers(PlayerPosition position, Integer age, String query, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(size, 50));
        String normalizedQuery = query == null || query.isBlank() ? null : query.trim();
        Page<Long> idsPage = playerProfileRepository.searchPublicPlayerIds(position, age, normalizedQuery,
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        if (idsPage.isEmpty()) return idsPage.map(id -> null);
        java.util.Map<Long, PlayerProfile> profilesById = playerProfileRepository.findAllByIdIn(idsPage.getContent()).stream()
                .collect(java.util.stream.Collectors.toMap(PlayerProfile::getId, profile -> profile));
        return idsPage.map(id -> PublicPlayerProfileDto.fromEntity(profilesById.get(id)));
    }

    private List<PlayerProfile> getMatchingProfiles(PlayerPosition position, Integer age) {
        if (position != null && age != null) return playerProfileRepository.findByPositionAndAge(position, age);
        if (position != null) return playerProfileRepository.findByPosition(position);
        if (age != null) return playerProfileRepository.findByAge(age);
        return playerProfileRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public PublicPlayerProfileDto getPublicPlayerById(Long id) {
        PlayerProfile profile = playerProfileRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("PlayerProfile", id.toString()));
        return PublicPlayerProfileDto.fromEntity(profile);
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
        profile.setUserId(userId);
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            profile.setFullName(request.getFullName());
        } else if (profile.getFullName() == null) {
            profile.setFullName(user.getFullName());
        }

        if (request.getAge() != null) {
            profile.setAge(request.getAge());
        }
        if (request.getBirthDate() != null) {
            profile.setBirthDate(request.getBirthDate());
            profile.setAge(java.time.Period.between(request.getBirthDate(), java.time.LocalDate.now()).getYears());
        }
        if (request.getNationality() != null) profile.setNationality(request.getNationality().trim());
        if (request.getCityCountry() != null) profile.setCityCountry(request.getCityCountry().trim());
        if (request.getCurrentTeam() != null) profile.setCurrentTeam(request.getCurrentTeam().trim());
        if (request.getCompetitionLevel() != null) profile.setCompetitionLevel(request.getCompetitionLevel());
        if (request.getPlayerGoal() != null) profile.setPlayerGoal(request.getPlayerGoal().trim());
        if (request.getAvailableForTransfer() != null) profile.setAvailableForTransfer(request.getAvailableForTransfer());
        if (request.getPosition() != null) {
            profile.setPosition(request.getPosition());
        }
        if (request.getSecondaryPositions() != null) {
            profile.setSecondaryPositions(new HashSet<>(request.getSecondaryPositions()));
        }
        if (request.getVideoUrls() != null || request.getUploadedVideoUrls() != null) {
            List<String> requestedVideos = new ArrayList<>();
            if (request.getVideoUrls() != null) requestedVideos.addAll(request.getVideoUrls());
            if (request.getUploadedVideoUrls() != null) {
                requestedVideos.addAll(request.getUploadedVideoUrls().stream().filter(url -> url != null && url.startsWith("platform-video:")).toList());
            }
            List<String> limitedUrls = requestedVideos.stream()
                    .filter(u -> u != null && !u.isBlank())
                    .map(String::trim)
                    .distinct()
                    .limit(3)
                    .collect(Collectors.toList());
            profile.setVideoUrls(limitedUrls);
            profile.setVideoUrl(limitedUrls.isEmpty() ? null : limitedUrls.get(0));
        } else if (request.getVideoUrl() != null) {
            String videoUrl = request.getVideoUrl().trim();
            profile.setVideoUrl(videoUrl.isBlank() ? null : videoUrl);
            profile.setVideoUrls(videoUrl.isBlank() ? new ArrayList<>() : new ArrayList<>(List.of(videoUrl)));
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
        if (request.getRemovedUploadedVideoUrls() != null && !request.getRemovedUploadedVideoUrls().isEmpty()) {
            // Object deletion is handled by the web storage endpoint after profile save.
        }
        return PlayerProfileDto.fromEntity(saved);
    }
}
