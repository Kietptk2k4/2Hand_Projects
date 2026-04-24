package com.twohands.authservice.domain.user;

import java.time.Instant;
import java.util.UUID;

public class UserSettings {

    private UUID userId;
    private AppearanceMode appearanceMode;
    private Instant createdAt;
    private Instant updatedAt;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public AppearanceMode getAppearanceMode() {
        return appearanceMode;
    }

    public void setAppearanceMode(AppearanceMode appearanceMode) {
        this.appearanceMode = appearanceMode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
