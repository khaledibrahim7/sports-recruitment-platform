package com.sports.web.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PlayerVideoStorageService {
    private static final long MAX_VIDEO_BYTES = 100L * 1024 * 1024;
    private static final long MAX_IMAGE_BYTES = 10L * 1024 * 1024;
    private static final Duration UPLOAD_URL_TTL = Duration.ofMinutes(10);
    private static final Map<String, String> VIDEO_TYPES = Map.of(
            "mp4", "video/mp4", "webm", "video/webm", "mov", "video/quicktime", "m4v", "video/x-m4v", "ogg", "video/ogg");
    private static final Map<String, String> IMAGE_TYPES = Map.of(
            "jpg", "image/jpeg", "jpeg", "image/jpeg", "png", "image/png", "webp", "image/webp", "avif", "image/avif");

    private final S3Client s3Client;
    private final S3Presigner presigner;
    private final String bucket;
    private final Duration playbackUrlTtl;

    public PlayerVideoStorageService(@Value("${app.storage.s3.bucket:}") String bucket,
                                     @Value("${app.storage.s3.region:auto}") String region,
                                     @Value("${app.storage.s3.endpoint:}") String endpoint,
                                     @Value("${app.storage.s3.access-key:}") String accessKey,
                                     @Value("${app.storage.s3.secret-key:}") String secretKey,
                                     @Value("${app.storage.s3.signed-url-ttl-seconds:3600}") long playbackUrlTtlSeconds) {
        this.bucket = bucket;
        this.playbackUrlTtl = Duration.ofSeconds(Math.max(60, Math.min(playbackUrlTtlSeconds, 604800)));
        Region awsRegion = "auto".equalsIgnoreCase(region) ? Region.US_EAST_1 : Region.of(region);
        var credentials = accessKey != null && !accessKey.isBlank() && secretKey != null && !secretKey.isBlank()
                ? StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey))
                : DefaultCredentialsProvider.create();
        var clientBuilder = S3Client.builder().region(awsRegion).credentialsProvider(credentials);
        var presignerBuilder = S3Presigner.builder().region(awsRegion).credentialsProvider(credentials);
        if (endpoint != null && !endpoint.isBlank()) {
            URI endpointUri = URI.create(endpoint);
            clientBuilder.endpointOverride(endpointUri).forcePathStyle(true);
            presignerBuilder.endpointOverride(endpointUri).serviceConfiguration(
                    software.amazon.awssdk.services.s3.S3Configuration.builder().pathStyleAccessEnabled(true).build());
        }
        this.s3Client = clientBuilder.build();
        this.presigner = presignerBuilder.build();
    }

    public String createPlaybackUrl(String requestedKey) {
        requireStorage();
        String key = requestedKey.startsWith("player-videos/") ? requestedKey : "player-videos/" + requestedKey;
        if (!key.matches("player-videos/[0-9]+/[a-fA-F0-9-]+\\.(mp4|webm|mov|m4v|ogg)")) {
            throw new IllegalArgumentException("Invalid video key");
        }
        var get = GetObjectRequest.builder().bucket(bucket).key(key).build();
        var request = GetObjectPresignRequest.builder().signatureDuration(playbackUrlTtl).getObjectRequest(get).build();
        return presigner.presignGetObject(request).url().toString();
    }

    public String createImageUrl(String requestedKey) {
        requireStorage();
        String key = requestedKey.startsWith("profile-images/") ? requestedKey : "profile-images/" + requestedKey;
        if (!key.matches("profile-images/[0-9]+/[a-fA-F0-9-]+\\.(jpg|jpeg|png|webp|avif)")) {
            throw new IllegalArgumentException("Invalid image key");
        }
        var get = GetObjectRequest.builder().bucket(bucket).key(key).build();
        var request = GetObjectPresignRequest.builder().signatureDuration(playbackUrlTtl).getObjectRequest(get).build();
        return presigner.presignGetObject(request).url().toString();
    }

    public UploadTarget createImageUploadUrl(Long playerId, String filename, String contentType) {
        requireStorage();
        String safeFilename = StringUtils.cleanPath(filename);
        String extension = StringUtils.getFilenameExtension(safeFilename);
        String expectedType = extension == null ? null : IMAGE_TYPES.get(extension.toLowerCase());
        if (safeFilename.contains("..") || expectedType == null || !expectedType.equalsIgnoreCase(contentType)) {
            throw new IllegalArgumentException("Invalid image format");
        }
        String key = "profile-images/" + playerId + "/" + UUID.randomUUID() + "." + extension.toLowerCase();
        var put = PutObjectRequest.builder().bucket(bucket).key(key).contentType(expectedType).build();
        var request = PutObjectPresignRequest.builder().signatureDuration(UPLOAD_URL_TTL).putObjectRequest(put).build();
        return new UploadTarget(key, presigner.presignPutObject(request).url().toString(), expectedType, MAX_IMAGE_BYTES);
    }

    public String confirmImageUpload(Long playerId, String key) {
        requireStorage();
        if (key == null || !key.matches("profile-images/" + playerId + "/[a-fA-F0-9-]+\\.(jpg|jpeg|png|webp|avif)")) {
            throw new IllegalArgumentException("Invalid uploaded image key");
        }
        HeadObjectResponse uploaded = s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(key).build());
        String extension = key.substring(key.lastIndexOf('.') + 1).toLowerCase();
        if (uploaded.contentLength() == null || uploaded.contentLength() <= 0 || uploaded.contentLength() > MAX_IMAGE_BYTES
                || !IMAGE_TYPES.get(extension).equalsIgnoreCase(uploaded.contentType())) {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
            throw new IllegalArgumentException("Image must be between 1 byte and 10 MB");
        }
        return "platform-image:" + key;
    }

    public void deleteVideo(Long playerId, String requestedKey) {
        requireStorage();
        String prefix = playerPrefix(playerId);
        String file = requestedKey.startsWith(prefix) ? requestedKey.substring(prefix.length()) : requestedKey;
        if (file.contains("/") || file.contains("..") || !file.matches("[a-fA-F0-9-]+\\.(mp4|webm|mov|m4v|ogg)")) {
            throw new IllegalArgumentException("Invalid video key");
        }
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(prefix + file).build());
    }

    public void deleteRemovedVideos(Long playerId, List<String> keys) {
        requireStorage();
        String prefix = playerPrefix(playerId);
        List<ObjectIdentifier> objects = keys == null ? List.of() : keys.stream()
                .filter(key -> key != null && key.startsWith(prefix)
                        && key.matches("player-videos/" + playerId + "/[a-fA-F0-9-]+\\.(mp4|webm|mov|m4v|ogg)"))
                .map(key -> ObjectIdentifier.builder().key(key).build()).toList();
        if (!objects.isEmpty()) {
            s3Client.deleteObjects(DeleteObjectsRequest.builder().bucket(bucket)
                    .delete(software.amazon.awssdk.services.s3.model.Delete.builder().objects(objects).quiet(true).build()).build());
        }
    }

    public UploadTarget createUploadUrl(Long playerId, String filename, String contentType) {
        requireStorage();
        String safeFilename = StringUtils.cleanPath(filename);
        String extension = StringUtils.getFilenameExtension(safeFilename);
        String expectedType = extension == null ? null : VIDEO_TYPES.get(extension.toLowerCase());
        if (safeFilename.contains("..") || expectedType == null || !expectedType.equalsIgnoreCase(contentType)) {
            throw new IllegalArgumentException("Invalid video format");
        }
        String key = playerPrefix(playerId) + UUID.randomUUID() + "." + extension.toLowerCase();
        var put = PutObjectRequest.builder().bucket(bucket).key(key).contentType(expectedType).build();
        var request = PutObjectPresignRequest.builder().signatureDuration(UPLOAD_URL_TTL).putObjectRequest(put).build();
        return new UploadTarget(key, presigner.presignPutObject(request).url().toString(), expectedType, MAX_VIDEO_BYTES);
    }

    public String confirmUpload(Long playerId, String key) {
        requireStorage();
        String prefix = playerPrefix(playerId);
        if (key == null || !key.startsWith(prefix) || key.contains("..") || key.length() > 220
                || !key.matches("player-videos/" + playerId + "/[a-fA-F0-9-]+\\.(mp4|webm|mov|m4v|ogg)")) {
            throw new IllegalArgumentException("Invalid uploaded video key");
        }
        HeadObjectResponse uploaded = s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(key).build());
        if (uploaded.contentLength() == null || uploaded.contentLength() <= 0 || uploaded.contentLength() > MAX_VIDEO_BYTES) {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
            throw new IllegalArgumentException("Video must be between 1 byte and 100 MB");
        }
        return "platform-video:" + key;
    }

    private String playerPrefix(Long playerId) { return "player-videos/" + playerId + "/"; }
    private void requireStorage() {
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("Video storage is not configured. Set VIDEO_S3_BUCKET and S3 credentials.");
        }
    }

    public record UploadTarget(String key, String uploadUrl, String contentType, long maxBytes) {}
}
