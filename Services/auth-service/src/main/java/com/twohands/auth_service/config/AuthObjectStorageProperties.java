package com.twohands.auth_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "auth.object-storage")
public class AuthObjectStorageProperties {

    private boolean enabled;
    private String endpoint = "http://localhost:9000";
    private String presignedEndpoint = "";
    private String publicUrl = "https://cdn.2hands.vn";
    private String accessKey = "admin";
    private String secretKey = "password123";
    private String avatarBucket = "2hands-avatar";
    private long avatarMaxFileSizeBytes = 5_242_880L;
    private int presignedUrlTtlSeconds = 900;
    private List<String> allowedAvatarContentTypes = List.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

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

    public String getPresignedEndpoint() {
        return presignedEndpoint;
    }

    public void setPresignedEndpoint(String presignedEndpoint) {
        this.presignedEndpoint = presignedEndpoint;
    }

    public String resolvePresignedEndpoint() {
        return MinioPresignEndpointResolver.resolve(endpoint, presignedEndpoint, publicUrl);
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

    public String getAvatarBucket() {
        return avatarBucket;
    }

    public void setAvatarBucket(String avatarBucket) {
        this.avatarBucket = avatarBucket;
    }

    public long getAvatarMaxFileSizeBytes() {
        return avatarMaxFileSizeBytes;
    }

    public void setAvatarMaxFileSizeBytes(long avatarMaxFileSizeBytes) {
        this.avatarMaxFileSizeBytes = avatarMaxFileSizeBytes;
    }

    public int getPresignedUrlTtlSeconds() {
        return presignedUrlTtlSeconds;
    }

    public void setPresignedUrlTtlSeconds(int presignedUrlTtlSeconds) {
        this.presignedUrlTtlSeconds = presignedUrlTtlSeconds;
    }

    public List<String> getAllowedAvatarContentTypes() {
        return allowedAvatarContentTypes;
    }

    public void setAllowedAvatarContentTypes(List<String> allowedAvatarContentTypes) {
        this.allowedAvatarContentTypes = allowedAvatarContentTypes;
    }
}
