package com.twohands.social_service.application.comment.replycomment;

import com.twohands.social_service.domain.comment.CommentMediaItem;

import java.util.List;
import java.util.UUID;

public record ReplyCommentCommand(
        UUID authorId,
        String parentCommentId,
        String contentText,
        List<CommentMediaItem> media
) {
}
