package com.twohands.auth_service.domain.rbac;

import java.util.Set;
import java.util.UUID;

public interface PermissionQueryRepository {
    Set<String> findPermissionCodesByRoleIds(Set<UUID> roleIds);

    Set<String> findPermissionCodesByUserId(UUID userId);
}
