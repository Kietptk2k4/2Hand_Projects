package com.twohands.auth_service.domain.rbac;

import java.util.Set;

public final class SystemRolePolicy {

    private static final Set<String> PROTECTED_ROLE_CODES = Set.of("USER", "ADMIN", "MODERATOR");

    private SystemRolePolicy() {
    }

    public static boolean isProtected(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            return false;
        }
        return PROTECTED_ROLE_CODES.contains(roleCode.trim().toUpperCase());
    }
}
