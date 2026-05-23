package com.twohands.auth_service.domain.rbac;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

public final class AdminPortalAccessPolicy {

    private static final Set<String> ADMIN_ROLE_CODES = Set.of(
            "ADMIN",
            "MODERATOR",
            "SUPPORT",
            "SUPER_ADMIN"
    );
    private static final String ADMIN_ACCESS_PERMISSION = "ADMIN_ACCESS";

    private AdminPortalAccessPolicy() {
    }

    public static boolean canAccessAdminPortal(Collection<String> roleCodes, Collection<String> permissionCodes) {
        if (roleCodes != null) {
            for (String roleCode : roleCodes) {
                if (roleCode != null && ADMIN_ROLE_CODES.contains(roleCode.toUpperCase(Locale.ROOT))) {
                    return true;
                }
            }
        }
        if (permissionCodes == null) {
            return false;
        }
        return permissionCodes.stream()
                .anyMatch(code -> ADMIN_ACCESS_PERMISSION.equalsIgnoreCase(code));
    }
}
