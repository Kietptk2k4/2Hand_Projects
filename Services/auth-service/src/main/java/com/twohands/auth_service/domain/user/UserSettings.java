package com.twohands.auth_service.domain.user;

import java.time.Instant;
import java.util.UUID;

public final class UserSettings {
    private final UUID userId;
    private AppearanceMode appearanceMode;
    private final Instant createdAt;
    private Instant updatedAt;

    private UserSettings(UUID userId, AppearanceMode appearanceMode, Instant createdAt, Instant updatedAt) {
        if (userId == null) {
            throw new UserDomainError("USER_ID_REQUIRED", "User id is required");
        }
        this.userId = userId;
        this.appearanceMode = appearanceMode;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UserSettings createDefault(UUID userId, Instant now) {
        return new UserSettings(userId, AppearanceMode.SYSTEM, now, now);
    }

    public static UserSettings rehydrate(UUID userId, AppearanceMode appearanceMode, Instant createdAt, Instant updatedAt) {
        return new UserSettings(userId, appearanceMode, createdAt, updatedAt);
    }

    public void updateAppearanceMode(AppearanceMode mode, Instant now) {
        if (mode == null) {
            throw new UserDomainError("USER_APPEARANCE_MODE_REQUIRED", "Appearance mode is required");
        }
        this.appearanceMode = mode;
        this.updatedAt = now;
    }

    public UUID userId() {
        return userId;
    }

    public AppearanceMode appearanceMode() {
        return appearanceMode;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
