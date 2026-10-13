package com.sports.web.controller;

import com.sports.common.security.UserPrincipal;
import com.sports.common.dto.ApiResponse;
import com.sports.player.domain.PlayerPosition;
import com.sports.player.dto.PlayerProfileDto;
import com.sports.player.dto.PlayerProfileUpdateRequest;
import com.sports.player.service.PlayerProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/players")
@Tag(name = "Players Catalog", description = "Player Profile Discovery & Recruitment Endpoints")
public class PlayerProfileController {

    private final PlayerProfileService playerProfileService;

    public PlayerProfileController(PlayerProfileService playerProfileService) {
        this.playerProfileService = playerProfileService;
    }

    @GetMapping
    @Operation(summary = "Get list of player profiles with optional position and age filtering")
    public ResponseEntity<ApiResponse<List<PlayerProfileDto>>> getAllPlayers(
            @RequestParam(required = false) PlayerPosition position,
            @RequestParam(required = false) Integer age) {
        List<PlayerProfileDto> players = playerProfileService.getAllPlayers(position, age);
        return ResponseEntity.ok(ApiResponse.ok(players));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get specific player profile details by ID")
    public ResponseEntity<ApiResponse<PlayerProfileDto>> getPlayerById(@PathVariable Long id) {
        PlayerProfileDto player = playerProfileService.getPlayerById(id);
        return ResponseEntity.ok(ApiResponse.ok(player));
    }

    @PutMapping("/me")
    @Operation(summary = "Update authenticated player's own profile data")
    public ResponseEntity<ApiResponse<PlayerProfileDto>> updateMyProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody PlayerProfileUpdateRequest request) {
        PlayerProfileDto updated = playerProfileService.createOrUpdateProfile(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Player profile updated successfully", updated));
    }
}
