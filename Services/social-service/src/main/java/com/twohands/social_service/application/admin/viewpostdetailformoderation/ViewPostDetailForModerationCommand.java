package com.twohands.social_service.application.admin.viewpostdetailformoderation;

import com.twohands.social_service.security.AuthenticatedUser;

public record ViewPostDetailForModerationCommand(
        AuthenticatedUser actor,
        String postId
) {
}
