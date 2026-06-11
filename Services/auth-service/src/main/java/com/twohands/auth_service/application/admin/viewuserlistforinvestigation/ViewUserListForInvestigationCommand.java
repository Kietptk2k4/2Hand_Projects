package com.twohands.auth_service.application.admin.viewuserlistforinvestigation;

import java.util.UUID;

public record ViewUserListForInvestigationCommand(
        UUID actorUserId,
        String status,
        String query,
        String sort,
        int page,
        int size
) {
}
