package com.twohands.social_service.application.admin.viewcommentlistformoderation;

import com.twohands.social_service.security.AuthenticatedUser;

public record ViewCommentListForModerationCommand(
        AuthenticatedUser actor,
        String status,
        String postId,
        String query,
        String sort,
        int page,
        int size
) {
}
