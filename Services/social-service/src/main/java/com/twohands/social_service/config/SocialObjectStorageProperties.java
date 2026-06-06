package com.twohands.social_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.UUID;

@ConfigurationProperties(prefix = "social.object-storage")
public class SocialObjectStorageProperties {

    private boolean enabled;
    private String endpoint = "http://localhost:9000";
    private String publicUrl = "https://cdn.2hands.vn";
    private String accessKey = "admin";
    private String secretKey = "password123";
    private String postBucket = "2hands-social-post";
    private String publicPathPrefix = "social";
    private long imageMaxFileSizeBytes = 10_485_760L;
    private long videoMaxFileSizeBytes = 104_857_600L;
    private int presignedUrlTtlSeconds = 900;
    private List<String> allowedImageContentTypes = List.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );
    private List<String> allowedVideoContentTypes = List.of("video/mp4");

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getPublicUrl() {
        return publicUrl;
    }

    public void setPublicUrl(String publicUrl) {
        this.publicUrl = publicUrl;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getPostBucket() {
        return postBucket;
    }

    public void setPostBucket(String postBucket) {
        this.postBucket = postBucket;
    }

    public String getPublicPathPrefix() {
        return publicPathPrefix;
    }

    public void setPublicPathPrefix(String publicPathPrefix) {
        this.publicPathPrefix = publicPathPrefix;
    }

    public long getImageMaxFileSizeBytes() {
        return imageMaxFileSizeBytes;
    }

    public void setImageMaxFileSizeBytes(long imageMaxFileSizeBytes) {
        this.imageMaxFileSizeBytes = imageMaxFileSizeBytes;
    }

    public long getVideoMaxFileSizeBytes() {
        return videoMaxFileSizeBytes;
    }

    public void setVideoMaxFileSizeBytes(long videoMaxFileSizeBytes) {
        this.videoMaxFileSizeBytes = videoMaxFileSizeBytes;
    }

    public int getPresignedUrlTtlSeconds() {
        return presignedUrlTtlSeconds;
    }

    public void setPresignedUrlTtlSeconds(int presignedUrlTtlSeconds) {
        this.presignedUrlTtlSeconds = presignedUrlTtlSeconds;
    }

    public List<String> getAllowedImageContentTypes() {
        return allowedImageContentTypes;
    }

    public void setAllowedImageContentTypes(List<String> allowedImageContentTypes) {
        this.allowedImageContentTypes = allowedImageContentTypes;
    }

    public List<String> getAllowedVideoContentTypes() {
        return allowedVideoContentTypes;
    }

    public void setAllowedVideoContentTypes(List<String> allowedVideoContentTypes) {
        this.allowedVideoContentTypes = allowedVideoContentTypes;
    }

    public List<String> allAllowedContentTypes() {
        java.util.ArrayList<String> combined = new java.util.ArrayList<>(allowedImageContentTypes);
        for (String type : allowedVideoContentTypes) {
            if (!combined.contains(type)) {
                combined.add(type);
            }
        }
        return List.copyOf(combined);
    }

    public String buildPublicObjectUrl(String objectKey) {
        String base = trimTrailingSlash(publicUrl);
        String prefix = normalizePathPrefix(publicPathPrefix);
        if (prefix.isEmpty()) {
            return base + "/" + objectKey;
        }
        return base + "/" + prefix + "/" + objectKey;
    }

    public String buildAllowedMediaUrlPrefix(UUID userId) {
        return buildPublicObjectUrl("posts/" + userId + "/");
    }

    private static String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String trimmed = value;
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private static String normalizePathPrefix(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String prefix = value;
        while (prefix.startsWith("/")) {
            prefix = prefix.substring(1);
        }
        while (prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        return prefix;
    }
}
