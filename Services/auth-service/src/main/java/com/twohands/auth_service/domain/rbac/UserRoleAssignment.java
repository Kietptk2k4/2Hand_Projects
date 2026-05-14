package com.twohands.auth_service.domain.rbac;

import com.twohands.auth_service.domain.shared.DomainEvent;
import com.twohands.auth_service.domain.rbac.event.UserRoleAssignedEvent;
import com.twohands.auth_service.domain.rbac.event.UserRoleRevokedEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class UserRoleAssignment {
    private final UUID userId;
    private final Set<UUID> roleIds;
    private final Instant createdAt;
    private Instant updatedAt;

    private final List<DomainEvent> pendingEvents = new ArrayList<>();

    public UserRoleAssignment(UUID userId, Set<UUID> roleIds, Instant createdAt, Instant updatedAt) {
        if (userId == null) {
            throw new RbacDomainError("RBAC_USER_ID_REQUIRED", "User id is required");
        }
        this.userId = userId;
        this.roleIds = roleIds == null ? new HashSet<>() : new HashSet<>(roleIds);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void assignRole(UUID roleId, Instant now) {
        if (roleId == null) {
            throw new RbacDomainError("RBAC_ROLE_ID_REQUIRED", "Role id is required");
        }
        boolean changed = roleIds.add(roleId);
        if (changed) {
            this.updatedAt = now;
            this.pendingEvents.add(new UserRoleAssignedEvent(userId, roleId, now));
        }
    }

    public void revokeRole(UUID roleId, Instant now) {
        if (roleId == null) {
            throw new RbacDomainError("RBAC_ROLE_ID_REQUIRED", "Role id is required");
        }
        boolean changed = roleIds.remove(roleId);
        if (changed) {
            this.updatedAt = now;
            this.pendingEvents.add(new UserRoleRevokedEvent(userId, roleId, now));
        }
    }

    public boolean hasRole(UUID roleId) {
        return roleIds.contains(roleId);
    }

    public Set<UUID> roleIds() {
        return Set.copyOf(roleIds);
    }

    public UUID userId() {
        return userId;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(pendingEvents);
        pendingEvents.clear();
        return events;
    }
}
