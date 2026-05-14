package com.twohands.auth_service.domain.rbac;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoleAssignmentDomainServiceTest {

    private final RoleAssignmentDomainService service = new RoleAssignmentDomainService();

    @Test
    void shouldRejectSelfAssignRole() {
        UUID actor = UUID.randomUUID();

        RbacDomainError error = assertThrows(
                RbacDomainError.class,
                () -> service.ensureCanAssignRole(actor, actor)
        );

        assertEquals("RBAC_SELF_ASSIGN_FORBIDDEN", error.code());
    }

    @Test
    void shouldRejectRevokingLastSuperAdmin() {
        UUID superAdminRoleId = UUID.randomUUID();

        RbacDomainError error = assertThrows(
                RbacDomainError.class,
                () -> service.ensureNotRevokingLastSuperAdmin(superAdminRoleId, superAdminRoleId, 1)
        );

        assertEquals("RBAC_LAST_SUPER_ADMIN_PROTECTED", error.code());
    }
}
