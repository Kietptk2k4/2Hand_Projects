package com.twohands.auth_service.domain.user;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public final class UserProfile {
    private final UUID userId;
    private String displayName;
    private String avatarUrl;
    private String bio;
    private String website;
    private Map<String, String> socialLinks;
    private boolean isPrivate;
    private final Instant createdAt;
    private Instant updatedAt;

    private UserProfile(
            UUID userId,
            String displayName,
            String avatarUrl,
            String bio,
            String website,
            Map<String, String> socialLinks,
            boolean isPrivate,
            Instant createdAt,
            Instant updatedAt
    ) {
        if (userId == null) {
            throw new UserDomainError("USER_ID_REQUIRED", "User id is required");
        }
        this.userId = userId;
        this.displayName = normalize(displayName);
        this.avatarUrl = normalizeNullable(avatarUrl);
        this.bio = normalizeNullable(bio);
        this.website = normalizeNullable(website);
        this.socialLinks = socialLinks == null ? Map.of() : Map.copyOf(socialLinks);
        this.isPrivate = isPrivate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UserProfile createDefault(UUID userId, String displayName, Instant now) {
        return new UserProfile(userId, displayName, null, null, null, Map.of(), false, now, now);
    }

    public void updateBasicInfo(String displayName, String bio, String website, Map<String, String> socialLinks, Instant now) {
        this.displayName = normalize(displayName);
        this.bio = normalizeNullable(bio);
        this.website = normalizeNullable(website);
        this.socialLinks = socialLinks == null ? Map.of() : Map.copyOf(socialLinks);
        this.updatedAt = now;
    }

    public void updateAvatar(String avatarUrl, Instant now) {
        this.avatarUrl = normalizeNullable(avatarUrl);
        this.updatedAt = now;
    }

    public void togglePrivacy(boolean isPrivate, Instant now) {
        this.isPrivate = isPrivate;
        this.updatedAt = now;
    }

    public UUID userId() {
        return userId;
    }

    public String displayName() {
        return displayName;
    }

    public String avatarUrl() {
        return avatarUrl;
    }

    public String bio() {
        return bio;
    }

    public String website() {
        return website;
    }

    public Map<String, String> socialLinks() {
        return socialLinks;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new UserDomainError("USER_DISPLAY_NAME_REQUIRED", "Display name is required");
        }
        return value.trim();
    }

    private static String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
