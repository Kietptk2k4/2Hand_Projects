package com.twohands.social_service.application.search.searchhashtag;

import com.twohands.social_service.domain.post.PageResult;

import java.util.List;

public record SearchHashtagResult(
        String hashtag,
        List<SearchHashtagPostItem> items,
        PageResultMeta meta
) {
    public static SearchHashtagResult from(String hashtag, PageResult<SearchHashtagPostItem> page) {
        return new SearchHashtagResult(
                hashtag,
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

    public record SearchHashtagPostItem(
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
