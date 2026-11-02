package com.sports.web.controller;

import com.sports.common.dto.ApiResponse;
import com.sports.common.security.UserPrincipal;
import com.sports.player.domain.PlayerPosition;
import com.sports.player.dto.PlayerProfileDto;
import com.sports.player.dto.PlayerProfileUpdateRequest;
import com.sports.player.dto.PublicPlayerProfileDto;
import com.sports.player.service.PlayerProfileService;
import com.sports.web.service.PlayerVideoStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/players")
@Tag(name = "Players Catalog", description = "Player Profile Discovery & Recruitment Endpoints")
public class PlayerProfileController {
    private final PlayerProfileService playerProfileService;
    private final PlayerVideoStorageService videoStorageService;

    public PlayerProfileController(PlayerProfileService playerProfileService, PlayerVideoStorageService videoStorageService) {
        this.playerProfileService = playerProfileService;
        this.videoStorageService = videoStorageService;
    }

    @GetMapping
    @Operation(summary = "Get list of player profiles with optional position and age filtering")
    public ResponseEntity<ApiResponse<PlayerSearchPage>> getAllPlayers(
            @RequestParam(required = false) PlayerPosition position,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Page<PublicPlayerProfileDto> result = playerProfileService.searchPublicPlayers(position, age, query, page, size);
        return ResponseEntity.ok(ApiResponse.ok(new PlayerSearchPage(result.getContent(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages())));
    }

    @GetMapping("/search")
    @Operation(summary = "Search player directory with server-side filters and pagination")
    public ResponseEntity<ApiResponse<PlayerSearchPage>> searchPlayers(
            @RequestParam(required = false) PlayerPosition position,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return getAllPlayers(position, age, query, page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get specific player profile details by ID")
    public ResponseEntity<ApiResponse<PublicPlayerProfileDto>> getPlayerById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(playerProfileService.getPublicPlayerById(id)));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PLAYER')")
    @Operation(summary = "Get authenticated player's private profile")
    public ResponseEntity<ApiResponse<PlayerProfileDto>> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(playerProfileService.getPlayerByUserId(principal.getId())));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('PLAYER')")
    @Operation(summary = "Update authenticated player's own profile data")
    public ResponseEntity<ApiResponse<PlayerProfileDto>> updateMyProfile(
            @AuthenticationPrincipal UserPrincipal principal, @RequestBody PlayerProfileUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Player profile updated successfully",
                playerProfileService.createOrUpdateProfile(principal.getId(), request)));
    }

    @GetMapping("/videos/{key:.+}")
    @Operation(summary = "Resolve a hosted video to a short-lived playback URL")
    public ResponseEntity<ApiResponse<String>> getVideoUrl(@PathVariable("key") String key) {
        return ResponseEntity.ok(ApiResponse.ok("Video playback URL", videoStorageService.createPlaybackUrl(key)));
    }

    @DeleteMapping("/me/videos/{key:.+}")
    @PreAuthorize("hasRole('PLAYER')")
    @Operation(summary = "Delete a hosted player video from object storage")
    public ResponseEntity<ApiResponse<Void>> deleteVideo(@AuthenticationPrincipal UserPrincipal principal,
                                                           @PathVariable("key") String key) {
        videoStorageService.deleteVideo(principal.getId(), key);
        return ResponseEntity.ok(ApiResponse.ok("Video deleted", null));
    }

    @DeleteMapping("/me/videos")
    @PreAuthorize("hasRole('PLAYER')")
    @Operation(summary = "Delete player-owned hosted videos removed from profile")
    public ResponseEntity<ApiResponse<Void>> deleteRemovedVideos(@AuthenticationPrincipal UserPrincipal principal,
                                                                  @RequestBody List<String> keys) {
        videoStorageService.deleteRemovedVideos(principal.getId(), keys);
        return ResponseEntity.ok(ApiResponse.ok("Removed videos deleted", null));
    }

    @PostMapping("/me/videos/upload-url")
    @PreAuthorize("hasRole('PLAYER')")
    @Operation(summary = "Create a short-lived upload URL for direct browser-to-object-storage upload")
    public ResponseEntity<ApiResponse<PlayerVideoStorageService.UploadTarget>> createVideoUploadUrl(
            @AuthenticationPrincipal UserPrincipal principal, @RequestParam String filename, @RequestParam String contentType) {
        return ResponseEntity.ok(ApiResponse.ok("Upload URL created",
                videoStorageService.createUploadUrl(principal.getId(), filename, contentType)));
    }

    @PostMapping("/me/videos/confirm")
    @PreAuthorize("hasRole('PLAYER')")
    @Operation(summary = "Confirm a direct object-storage upload and return the stored video reference")
    public ResponseEntity<ApiResponse<String>> confirmVideoUpload(@AuthenticationPrincipal UserPrincipal principal,
                                                                   @RequestParam String key) {
        return ResponseEntity.ok(ApiResponse.ok("Video upload confirmed",
                videoStorageService.confirmUpload(principal.getId(), key)));
    }

    public record PlayerSearchPage(List<PublicPlayerProfileDto> items, int page, int size,
                                   long totalElements, int totalPages) {}
}
