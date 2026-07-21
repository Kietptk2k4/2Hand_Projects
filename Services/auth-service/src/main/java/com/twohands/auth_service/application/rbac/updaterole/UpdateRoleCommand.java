package com.twohands.auth_service.application.rbac.updaterole;

import java.util.UUID;

public record UpdateRoleCommand(
        UUID actorUserId,
        UUID roleId,
        String name
) {
}
