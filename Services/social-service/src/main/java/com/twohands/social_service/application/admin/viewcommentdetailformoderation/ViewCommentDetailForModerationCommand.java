package com.twohands.social_service.application.admin.viewcommentdetailformoderation;

import com.twohands.social_service.security.AuthenticatedUser;

public record ViewCommentDetailForModerationCommand(
        AuthenticatedUser actor,
        String commentId
) {
}
