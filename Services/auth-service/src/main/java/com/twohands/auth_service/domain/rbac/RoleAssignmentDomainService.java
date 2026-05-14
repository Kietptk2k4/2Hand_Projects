package com.twohands.auth_service.domain.rbac;

import java.util.UUID;

public final class RoleAssignmentDomainService {

    public void ensureCanAssignRole(UUID actorUserId, UUID targetUserId) {
        if (actorUserId != null && actorUserId.equals(targetUserId)) {
            throw new RbacDomainError("RBAC_SELF_ASSIGN_FORBIDDEN", "User cannot assign role to self");
        }
    }

    public void ensureCanRevokeRole(UUID actorUserId, UUID targetUserId) {
        if (actorUserId != null && actorUserId.equals(targetUserId)) {
            throw new RbacDomainError("RBAC_SELF_REVOKE_FORBIDDEN", "User cannot revoke own role directly");
        }
    }

    public void ensureNotRevokingLastSuperAdmin(
            UUID roleId,
            UUID superAdminRoleId,
            long totalUsersHavingRole
    ) {
        if (roleId.equals(superAdminRoleId) && totalUsersHavingRole <= 1) {
            throw new RbacDomainError("RBAC_LAST_SUPER_ADMIN_PROTECTED", "Cannot revoke role from last super admin");
        }
    }
}
