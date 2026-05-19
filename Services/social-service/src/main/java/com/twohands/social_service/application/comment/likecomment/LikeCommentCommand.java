package com.twohands.social_service.application.comment.likecomment;

import java.util.UUID;

public record LikeCommentCommand(
        UUID userId,
        String commentId
) {
}
