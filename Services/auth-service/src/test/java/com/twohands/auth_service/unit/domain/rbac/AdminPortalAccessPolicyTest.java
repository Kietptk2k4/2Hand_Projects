package com.twohands.auth_service.unit.domain.rbac;

import com.twohands.auth_service.domain.rbac.AdminPortalAccessPolicy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminPortalAccessPolicyTest {

    @Test
    void shouldAllowAdminRole() {
        assertTrue(AdminPortalAccessPolicy.canAccessAdminPortal(List.of("ADMIN"), List.of()));
    }

    @Test
    void shouldAllowAdminAccessPermission() {
        assertTrue(AdminPortalAccessPolicy.canAccessAdminPortal(List.of(), List.of("ADMIN_ACCESS")));
    }

    @Test
    void shouldDenyRegularUser() {
        assertFalse(AdminPortalAccessPolicy.canAccessAdminPortal(List.of("USER"), List.of("ORDER_READ")));
    }
}
