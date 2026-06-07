package com.twohands.social_service.application.comment.listpostcomments;

import com.twohands.social_service.domain.post.PageResult;

import java.util.List;

public record ListPostCommentsResult(
        List<CommentItem> items,
        PageResultMeta meta
) {
    public static ListPostCommentsResult from(PageResult<CommentItem> page) {
        return new ListPostCommentsResult(
                page.items(),
                new PageResultMeta(
                        page.page(),
                        page.size(),
                        page.totalElements(),
                        page.totalPages(),
                        page.hasNext()
                )
        );
    }

    public record CommentItem(
            String commentId,
            String postId,
            String parentCommentId,
            AuthorSummary author,
            String contentText,
            List<MediaItemData> media,
            long likeCount,
            boolean likedByMe,
            long replyCount,
            String createdAt,
            String updatedAt
    ) {
    }

    public record AuthorSummary(
            String userId,
            String displayName,
            String avatarUrl
    ) {
    }

    public record MediaItemData(
            String url,
            String type
    ) {
    }

    public record PageResultMeta(
            long page,
            long size,
            long totalElements,
            long totalPages,
            boolean hasNext
    ) {
    }
}
