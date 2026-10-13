package com.sports.web.controller;

import com.sports.common.dto.ApiResponse;
import com.sports.common.security.UserPrincipal;
import com.sports.player.dto.PlayerProfileDto;
import com.sports.player.service.PlayerBookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bookmarks")
@Tag(name = "Scout Bookmarks", description = "Endpoints for managing Scout Watchlists")
public class PlayerBookmarkController {

    private final PlayerBookmarkService bookmarkService;

    public PlayerBookmarkController(PlayerBookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @PostMapping("/{playerId}/toggle")
    @PreAuthorize("hasAnyRole('ACADEMY', 'CLUB')")
    @Operation(summary = "Toggle bookmarking a player (add or remove from watchlist)")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> toggleBookmark(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long playerId) {
        boolean bookmarked = bookmarkService.toggleBookmark(userPrincipal.getId(), playerId);
        String message = bookmarked ? "تمت إضافة اللاعب إلى قائمة المتابعة بنجاح" : "تم إزالة اللاعب من قائمة المتابعة";
        return ResponseEntity.ok(ApiResponse.ok(message, Map.of("bookmarked", bookmarked)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ACADEMY', 'CLUB')")
    @Operation(summary = "Get list of all players saved in scout's watchlist")
    public ResponseEntity<ApiResponse<List<PlayerProfileDto>>> getBookmarkedPlayers(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<PlayerProfileDto> players = bookmarkService.getBookmarkedPlayers(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(players));
    }

    @GetMapping("/ids")
    @PreAuthorize("hasAnyRole('ACADEMY', 'CLUB')")
    @Operation(summary = "Get list of player IDs currently saved in scout's watchlist")
    public ResponseEntity<ApiResponse<List<Long>>> getBookmarkedPlayerIds(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<Long> ids = bookmarkService.getBookmarkedPlayerIds(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(ids));
    }
}
