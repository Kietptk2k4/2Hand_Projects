package com.twohands.auth_service.domain.rbac;

import java.time.Instant;
import java.util.UUID;

public final class Permission {
    private final UUID id;
    private final String code;
    private String description;
    private final Instant createdAt;
    private Instant updatedAt;

    public Permission(UUID id, String code, String description, Instant createdAt, Instant updatedAt) {
        if (id == null || code == null || code.isBlank()) {
            throw new RbacDomainError("RBAC_PERMISSION_REQUIRED", "Permission id and code are required");
        }
        this.id = id;
        this.code = code.trim();
        this.description = description == null ? null : description.trim();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void updateDescription(String description, Instant now) {
        this.description = description == null ? null : description.trim();
        this.updatedAt = now;
    }

    public UUID id() {
        return id;
    }

    public String code() {
        return code;
    }

    public String description() {
        return description;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
