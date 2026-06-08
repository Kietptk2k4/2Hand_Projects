package com.twohands.social_service.application.post.viewsavedposts;

import com.twohands.social_service.domain.post.PageResult;

import java.util.List;

public record ViewSavedPostsResult(
        List<SavedPostItem> items,
        PageResultMeta meta
) {
    public static ViewSavedPostsResult from(PageResult<SavedPostItem> result) {
        return new ViewSavedPostsResult(
                result.items(),
                new PageResultMeta(
                        result.page(),
                        result.size(),
                        result.totalElements(),
                        result.totalPages(),
                        result.hasNext()
                )
        );
    }

    public record SavedPostItem(
            String postId,
            String authorId,
            String caption,
            List<MediaItemData> media,
            String visibility,
            long likeCount,
            long replyCount,
            List<String> hashtags,
            boolean allowComments,
            String savedAt,
            String createdAt,
            String updatedAt
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
