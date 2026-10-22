package com.sports.web.controller;

import com.sports.common.dto.ApiResponse;
import com.sports.common.security.UserPrincipal;
import com.sports.player.domain.PlayerPosition;
import com.sports.player.dto.PlayerProfileDto;
import com.sports.player.dto.PlayerProfileUpdateRequest;
import com.sports.player.dto.PublicPlayerProfileDto;
import com.sports.player.service.PlayerProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import org.springframework.data.domain.Page;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/players")
@Tag(name = "Players Catalog", description = "Player Profile Discovery & Recruitment Endpoints")
public class PlayerProfileController {

    private static final long MAX_VIDEO_BYTES = 100L * 1024 * 1024;

    private static final java.util.Map<String, String> VIDEO_TYPES = java.util.Map.of(
            "mp4", "video/mp4", "webm", "video/webm", "mov", "video/quicktime", "m4v", "video/x-m4v", "ogg", "video/ogg");
    private final PlayerProfileService playerProfileService;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;
    private final Duration signedUrlTtl;

    public PlayerProfileController(PlayerProfileService playerProfileService,
                                   @Value("${app.storage.s3.bucket:}") String bucket,
                                   @Value("${app.storage.s3.region:auto}") String region,
                                   @Value("${app.storage.s3.endpoint:}") String endpoint,
                                   @Value("${app.storage.s3.access-key:}") String accessKey,
                                   @Value("${app.storage.s3.secret-key:}") String secretKey,
                                   @Value("${app.storage.s3.signed-url-ttl-seconds:3600}") long signedUrlTtlSeconds) {
        this.playerProfileService = playerProfileService;
        this.bucket = bucket;
        this.signedUrlTtl = Duration.ofSeconds(Math.max(60, Math.min(signedUrlTtlSeconds, 604800)));
        Region awsRegion = "auto".equalsIgnoreCase(region) ? Region.US_EAST_1 : Region.of(region);
        var credentials = accessKey != null && !accessKey.isBlank() && secretKey != null && !secretKey.isBlank()
                ? StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey))
                : DefaultCredentialsProvider.create();
        var s3Builder = S3Client.builder().region(awsRegion).credentialsProvider(credentials);
        var presignerBuilder = S3Presigner.builder().region(awsRegion).credentialsProvider(credentials);
        if (endpoint != null && !endpoint.isBlank()) {
            URI endpointUri = URI.create(endpoint);
            s3Builder.endpointOverride(endpointUri).forcePathStyle(true);
            presignerBuilder.endpointOverride(endpointUri).serviceConfiguration(
                    software.amazon.awssdk.services.s3.S3Configuration.builder().pathStyleAccessEnabled(true).build());
        }
        this.s3Client = s3Builder.build();
        this.s3Presigner = presignerBuilder.build();
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
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody PlayerProfileUpdateRequest request) {
        PlayerProfileDto updated = playerProfileService.createOrUpdateProfile(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Player profile updated successfully", updated));
    }

    @GetMapping("/videos/{key:.+}")
    @Operation(summary = "Resolve a hosted video to a short-lived playback URL")
    public ResponseEntity<ApiResponse<String>> getVideoUrl(@PathVariable("key") String key) {
        requireStorage();
        String objectKey = key.startsWith("player-videos/") ? key : "player-videos/" + key;
        if (!objectKey.matches("player-videos/[0-9]+/[a-fA-F0-9-]+\\.(mp4|webm|mov|m4v|ogg)")) {
            throw new IllegalArgumentException("Invalid video key");
        }
        var request = software.amazon.awssdk.services.s3.model.GetObjectRequest.builder().bucket(bucket).key(objectKey).build();
        var presign = GetObjectPresignRequest.builder().signatureDuration(signedUrlTtl).getObjectRequest(request).build();
        return ResponseEntity.ok(ApiResponse.ok("Video playback URL", s3Presigner.presignGetObject(presign).url().toString()));
    }

    @DeleteMapping("/me/videos/{key:.+}")
    @Operation(summary = "Delete a hosted player video from object storage")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<ApiResponse<Void>> deleteVideo(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("key") String key) {
        requireStorage();
        String prefix = "player-videos/" + userPrincipal.getId() + "/";
        String fileKey = key.startsWith(prefix) ? key.substring(prefix.length()) : key;
        if (fileKey.contains("/") || fileKey.contains("..") || !fileKey.matches("[a-fA-F0-9-]+\\.(mp4|webm|mov|m4v|ogg)")) {
            throw new IllegalArgumentException("Invalid video key");
        }
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(prefix + fileKey).build());
        return ResponseEntity.ok(ApiResponse.ok("Video deleted", null));
    }

    @DeleteMapping("/me/videos")
    @Operation(summary = "Delete player-owned hosted videos removed from profile")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<ApiResponse<Void>> deleteRemovedVideos(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody List<String> keys) {
        requireStorage();
        String prefix = "player-videos/" + userPrincipal.getId() + "/";
        List<software.amazon.awssdk.services.s3.model.ObjectIdentifier> objects = keys == null ? List.of() : keys.stream()
                .filter(key -> key != null && key.startsWith(prefix)
                        && key.matches("player-videos/" + userPrincipal.getId() + "/[a-fA-F0-9-]+\\.(mp4|webm|mov|m4v|ogg)"))
                .map(key -> software.amazon.awssdk.services.s3.model.ObjectIdentifier.builder().key(key).build())
                .toList();
        if (!objects.isEmpty()) {
            s3Client.deleteObjects(software.amazon.awssdk.services.s3.model.DeleteObjectsRequest.builder().bucket(bucket)
                    .delete(software.amazon.awssdk.services.s3.model.Delete.builder().objects(objects).quiet(true).build())
                    .build());
        }
        return ResponseEntity.ok(ApiResponse.ok("Removed videos deleted", null));
    }

    @PostMapping("/me/videos/upload-url")
    @Operation(summary = "Create a short-lived upload URL for direct browser-to-S3 upload")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<ApiResponse<VideoUploadTarget>> createVideoUploadUrl(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam String filename,
            @RequestParam String contentType) {
        requireStorage();
        String safeFilename = StringUtils.cleanPath(filename);
        String extension = StringUtils.getFilenameExtension(safeFilename);
        String expectedType = extension == null ? null : VIDEO_TYPES.get(extension.toLowerCase());
        if (safeFilename.contains("..") || expectedType == null || !expectedType.equalsIgnoreCase(contentType)) {
            throw new IllegalArgumentException("Invalid video format");
        }
        String key = "player-videos/" + userPrincipal.getId() + "/" + UUID.randomUUID() + "." + extension.toLowerCase();
        var request = PutObjectRequest.builder().bucket(bucket).key(key).contentType(expectedType).build();
        var presigned = s3Presigner.presignPutObject(PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10)).putObjectRequest(request).build());
        return ResponseEntity.ok(ApiResponse.ok("Upload URL created", new VideoUploadTarget(key, presigned.url().toString(), expectedType, MAX_VIDEO_BYTES)));
    }

    @PostMapping("/me/videos/confirm")
    @Operation(summary = "Confirm a direct object-storage upload and return the stored video reference")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<ApiResponse<String>> confirmVideoUpload(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam String key) {
        requireStorage();
        String prefix = "player-videos/" + userPrincipal.getId() + "/";
        if (!key.startsWith(prefix) || key.contains("..") || key.length() > 220) {
            throw new IllegalArgumentException("Invalid uploaded video key");
        }
        HeadObjectResponse uploaded = s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(key).build());
        if (uploaded.contentLength() == null || uploaded.contentLength() <= 0 || uploaded.contentLength() > MAX_VIDEO_BYTES) {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
            throw new IllegalArgumentException("Video must be between 1 byte and 100 MB");
        }
        return ResponseEntity.ok(ApiResponse.ok("Video upload confirmed", "platform-video:" + key));
    }

    private void requireStorage() {
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("Video storage is not configured. Set VIDEO_S3_BUCKET and S3 credentials.");
        }
    }

    public record VideoUploadTarget(String key, String uploadUrl, String contentType, long maxBytes) {}
    public record PlayerSearchPage(List<PublicPlayerProfileDto> items, int page, int size, long totalElements, int totalPages) {}
}
