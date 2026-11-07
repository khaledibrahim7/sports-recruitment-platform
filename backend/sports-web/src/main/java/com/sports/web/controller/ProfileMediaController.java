package com.sports.web.controller;

import com.sports.common.dto.ApiResponse;
import com.sports.common.security.UserPrincipal;
import com.sports.web.service.PlayerVideoStorageService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Uploads profile images for all profile roles; object access stays in the storage service. */
@RestController
@RequestMapping("/api/v1/profile-media")
public class ProfileMediaController {
    private final PlayerVideoStorageService mediaStorage;

    public ProfileMediaController(PlayerVideoStorageService mediaStorage) {
        this.mediaStorage = mediaStorage;
    }

    @GetMapping("/images")
    @Operation(summary = "Resolve a profile image to a short-lived object-storage URL")
    public ResponseEntity<Void> resolveImage(@RequestParam String key) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .cacheControl(CacheControl.maxAge(java.time.Duration.ofMinutes(5)).cachePrivate())
                .location(java.net.URI.create(mediaStorage.createImageUrl(key))).build();
    }

    @PostMapping("/images/upload-url")
    @PreAuthorize("hasAnyRole('PLAYER', 'ACADEMY', 'CLUB')")
    @Operation(summary = "Create a short-lived upload URL for a profile image")
    public ResponseEntity<ApiResponse<PlayerVideoStorageService.UploadTarget>> createUploadUrl(
            @AuthenticationPrincipal UserPrincipal principal, @RequestParam String filename, @RequestParam String contentType) {
        return ResponseEntity.ok(ApiResponse.ok("Image upload URL created",
                mediaStorage.createImageUploadUrl(principal.getId(), filename, contentType)));
    }

    @PostMapping("/images/confirm")
    @PreAuthorize("hasAnyRole('PLAYER', 'ACADEMY', 'CLUB')")
    @Operation(summary = "Confirm an uploaded profile image")
    public ResponseEntity<ApiResponse<String>> confirmUpload(@AuthenticationPrincipal UserPrincipal principal,
                                                               @RequestParam String key) {
        return ResponseEntity.ok(ApiResponse.ok("Image upload confirmed",
                mediaStorage.confirmImageUpload(principal.getId(), key)));
    }
}
