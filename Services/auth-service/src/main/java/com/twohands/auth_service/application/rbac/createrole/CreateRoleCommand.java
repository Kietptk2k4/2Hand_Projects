package com.twohands.auth_service.application.rbac.createrole;

import java.util.UUID;

public record CreateRoleCommand(
        UUID actorUserId,
        String code,
        String name
) {
}
