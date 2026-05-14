package com.twohands.auth_service.domain.rbac;

import com.twohands.auth_service.domain.rbac.event.RolePermissionAssignedEvent;
import com.twohands.auth_service.domain.rbac.event.RolePermissionRevokedEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleTest {

    @Test
    void shouldAssignAndRevokePermissionWithEvents() {
        Instant now = Instant.now();
        UUID roleId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();

        Role role = new Role(roleId, "ADMIN", "Admin", Set.of(), now, now);

        role.assignPermission(permissionId, now.plusSeconds(10));
        assertTrue(role.hasPermission(permissionId));
        assertInstanceOf(RolePermissionAssignedEvent.class, role.pullDomainEvents().getFirst());

        role.revokePermission(permissionId, now.plusSeconds(20));
        assertEquals(false, role.hasPermission(permissionId));
        assertInstanceOf(RolePermissionRevokedEvent.class, role.pullDomainEvents().getFirst());
    }
}
