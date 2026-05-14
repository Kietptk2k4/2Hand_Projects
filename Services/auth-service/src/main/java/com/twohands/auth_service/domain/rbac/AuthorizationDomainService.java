package com.twohands.auth_service.domain.rbac;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class AuthorizationDomainService {

    public Set<String> aggregatePermissionCodes(PermissionQueryRepository permissionQueryRepository, Set<UUID> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Set.of();
        }
        return Set.copyOf(new HashSet<>(permissionQueryRepository.findPermissionCodesByRoleIds(roleIds)));
    }

    public boolean hasPermission(Set<String> permissionCodes, String requiredPermissionCode) {
        if (requiredPermissionCode == null || requiredPermissionCode.isBlank()) {
            throw new RbacDomainError("RBAC_PERMISSION_CODE_REQUIRED", "Required permission code is missing");
        }
        return permissionCodes != null && permissionCodes.contains(requiredPermissionCode.trim());
    }
}
