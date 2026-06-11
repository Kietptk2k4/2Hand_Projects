package com.twohands.social_service.application.admin.viewpostlistformoderation;

import com.twohands.social_service.security.AuthenticatedUser;

public record ViewPostListForModerationCommand(
        AuthenticatedUser actor,
        String status,
        String moderationStatus,
        String query,
        String sort,
        int page,
        int size
) {
}
