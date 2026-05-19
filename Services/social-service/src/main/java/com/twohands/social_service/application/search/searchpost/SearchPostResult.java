package com.twohands.social_service.application.search.searchpost;

import com.twohands.social_service.domain.post.PageResult;

import java.util.List;

public record SearchPostResult(
        String keyword,
        List<SearchPostItem> items,
        PageResultMeta meta
) {
    public static SearchPostResult from(String keyword, PageResult<SearchPostItem> page) {
        return new SearchPostResult(
                keyword,
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

    public record SearchPostItem(
            String postId,
            String authorId,
            String caption,
            List<MediaItemData> media,
            String visibility,
            long likeCount,
            long replyCount,
            List<String> hashtags,
            boolean allowComments,
            String createdAt,
            String updatedAt
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
