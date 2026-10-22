package com.sports.player.service;

import com.sports.player.domain.PlayerPosition;
import com.sports.player.dto.PlayerProfileDto;
import com.sports.player.dto.PublicPlayerProfileDto;
import com.sports.player.dto.PlayerProfileUpdateRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PlayerProfileService {
    List<PlayerProfileDto> getAllPlayers(PlayerPosition position, Integer age);
    List<PublicPlayerProfileDto> getPublicPlayers(PlayerPosition position, Integer age);
    Page<PublicPlayerProfileDto> searchPublicPlayers(PlayerPosition position, Integer age, String query, int page, int size);
    PlayerProfileDto getPlayerById(Long id);
    PublicPlayerProfileDto getPublicPlayerById(Long id);
    PlayerProfileDto getPlayerByUserId(Long userId);
    PlayerProfileDto createOrUpdateProfile(Long userId, PlayerProfileUpdateRequest request);
}
