package com.twohands.auth_service.application.rbac.deleterole;

import java.util.UUID;

public record DeleteRoleResult(
        UUID id,
        String code
) {
}
