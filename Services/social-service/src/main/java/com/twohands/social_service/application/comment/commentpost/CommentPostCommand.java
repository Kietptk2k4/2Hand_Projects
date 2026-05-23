package com.twohands.social_service.application.comment.commentpost;

import com.twohands.social_service.domain.comment.CommentMediaItem;

import java.util.List;
import java.util.UUID;

public record CommentPostCommand(
        UUID authorId,
        String postId,
        String contentText,
        List<CommentMediaItem> media
) {
}
