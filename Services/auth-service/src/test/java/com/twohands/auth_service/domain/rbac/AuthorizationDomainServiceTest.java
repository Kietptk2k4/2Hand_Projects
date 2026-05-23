package com.twohands.auth_service.domain.rbac;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthorizationDomainServiceTest {

    private final AuthorizationDomainService service = new AuthorizationDomainService();

    @Test
    void shouldAggregateDistinctPermissionCodes() {
        PermissionQueryRepository repository = new PermissionQueryRepository() {
            @Override
            public Set<String> findPermissionCodesByRoleIds(Set<UUID> roleIds) {
                return Set.of("USER_READ", "USER_UPDATE");
            }

            @Override
            public Set<String> findPermissionCodesByUserId(UUID userId) {
                return Set.of();
            }

            @Override
            public java.util.List<String> findRoleCodesByUserId(UUID userId) {
                return java.util.List.of();
            }

            @Override
            public java.util.List<PermissionQueryRepository.PermissionData> findPermissionsByRoleId(UUID roleId) {
                return java.util.List.of();
            }
        };

        Set<String> permissions = service.aggregatePermissionCodes(repository, Set.of(UUID.randomUUID()));

        assertEquals(2, permissions.size());
        assertFalse(service.hasPermission(permissions, "USER_DELETE"));
    }

    @Test
    void shouldRejectBlankRequiredPermissionCode() {
        RbacDomainError error = assertThrows(
                RbacDomainError.class,
                () -> service.hasPermission(Set.of("USER_READ"), "   ")
        );

        assertEquals("RBAC_PERMISSION_CODE_REQUIRED", error.code());
    }
}
