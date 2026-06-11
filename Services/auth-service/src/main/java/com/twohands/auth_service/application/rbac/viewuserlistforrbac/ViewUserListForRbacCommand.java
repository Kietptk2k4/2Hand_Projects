package com.twohands.auth_service.application.rbac.viewuserlistforrbac;

import java.util.UUID;

public record ViewUserListForRbacCommand(
        UUID actorUserId,
        String status,
        String query,
        String sort,
        int page,
        int size
) {
}
