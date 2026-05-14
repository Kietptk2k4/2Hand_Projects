package com.twohands.auth_service.domain.rbac;

import com.twohands.auth_service.domain.shared.DomainEvent;
import com.twohands.auth_service.domain.rbac.event.RolePermissionAssignedEvent;
import com.twohands.auth_service.domain.rbac.event.RolePermissionRevokedEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class Role {
    private final UUID id;
    private final String code;
    private String name;
    private final Set<UUID> permissionIds;
    private final Instant createdAt;
    private Instant updatedAt;

    private final List<DomainEvent> pendingEvents = new ArrayList<>();

    public Role(UUID id, String code, String name, Set<UUID> permissionIds, Instant createdAt, Instant updatedAt) {
        if (id == null || code == null || code.isBlank() || name == null || name.isBlank()) {
            throw new RbacDomainError("RBAC_ROLE_REQUIRED", "Role id, code, and name are required");
        }
        this.id = id;
        this.code = code.trim();
        this.name = name.trim();
        this.permissionIds = permissionIds == null ? new HashSet<>() : new HashSet<>(permissionIds);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void rename(String name, Instant now) {
        if (name == null || name.isBlank()) {
            throw new RbacDomainError("RBAC_ROLE_NAME_REQUIRED", "Role name is required");
        }
        this.name = name.trim();
        this.updatedAt = now;
    }

    public void assignPermission(UUID permissionId, Instant now) {
        if (permissionId == null) {
            throw new RbacDomainError("RBAC_PERMISSION_ID_REQUIRED", "Permission id is required");
        }
        boolean changed = this.permissionIds.add(permissionId);
        if (changed) {
            this.updatedAt = now;
            this.pendingEvents.add(new RolePermissionAssignedEvent(id, permissionId, now));
        }
    }

    public void revokePermission(UUID permissionId, Instant now) {
        if (permissionId == null) {
            throw new RbacDomainError("RBAC_PERMISSION_ID_REQUIRED", "Permission id is required");
        }
        boolean changed = this.permissionIds.remove(permissionId);
        if (changed) {
            this.updatedAt = now;
            this.pendingEvents.add(new RolePermissionRevokedEvent(id, permissionId, now));
        }
    }

    public boolean hasPermission(UUID permissionId) {
        return permissionIds.contains(permissionId);
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(pendingEvents);
        pendingEvents.clear();
        return events;
    }

    public UUID id() {
        return id;
    }

    public String code() {
        return code;
    }

    public String name() {
        return name;
    }

    public Set<UUID> permissionIds() {
        return Set.copyOf(permissionIds);
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
