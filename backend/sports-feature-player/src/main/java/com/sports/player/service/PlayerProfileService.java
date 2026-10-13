package com.sports.player.service;

import com.sports.player.domain.PlayerPosition;
import com.sports.player.dto.PlayerProfileDto;
import com.sports.player.dto.PlayerProfileUpdateRequest;

import java.util.List;

public interface PlayerProfileService {
    List<PlayerProfileDto> getAllPlayers(PlayerPosition position, Integer age);
    PlayerProfileDto getPlayerById(Long id);
    PlayerProfileDto getPlayerByUserId(Long userId);
    PlayerProfileDto createOrUpdateProfile(Long userId, PlayerProfileUpdateRequest request);
}
