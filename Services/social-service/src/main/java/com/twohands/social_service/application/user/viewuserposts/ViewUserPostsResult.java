package com.twohands.social_service.application.user.viewuserposts;

import com.twohands.social_service.domain.post.PageResult;

import java.util.List;

public record ViewUserPostsResult(
        List<UserPostItem> items,
        PageResultMeta meta
) {
    public static ViewUserPostsResult from(PageResult<UserPostItem> page) {
        return new ViewUserPostsResult(
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

    public record UserPostItem(
            String postId,
            String caption,
            List<MediaItemData> media,
            String visibility,
            long likeCount,
            long replyCount,
            List<String> hashtags,
            String createdAt
    ) {
    }

    public record MediaItemData(
            String url,
            String type,
            Integer width,
            Integer height
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
