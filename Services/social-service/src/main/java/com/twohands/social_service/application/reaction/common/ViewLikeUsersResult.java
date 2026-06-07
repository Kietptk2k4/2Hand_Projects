package com.twohands.social_service.application.reaction.common;

import com.twohands.social_service.domain.post.PageResult;

import java.util.List;

public record ViewLikeUsersResult(
        List<LikeUserItem> items,
        PageMeta meta
) {
    public static ViewLikeUsersResult from(PageResult<LikeUserItem> page) {
        return new ViewLikeUsersResult(
                page.items(),
                new PageMeta(
                        page.page(),
                        page.size(),
                        page.totalElements(),
                        page.totalPages(),
                        page.hasNext()
                )
        );
    }

    public record LikeUserItem(
            String userId,
            String displayName,
            String avatarUrl,
            String likedAt
    ) {
    }

    public record PageMeta(
            long page,
            long size,
            long totalElements,
            long totalPages,
            boolean hasNext
    ) {
    }
}