package com.twohands.social_service.application.feed.viewglobalfeed;

import com.twohands.social_service.domain.post.PageResult;

import java.math.BigDecimal;
import java.util.List;

public record ViewGlobalFeedResult(
        List<FeedPostItem> items,
        PageResultMeta meta
) {
    public static ViewGlobalFeedResult from(PageResult<FeedPostItem> result) {
        return new ViewGlobalFeedResult(
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

    public record FeedPostItem(
            String postId,
            String authorId,
            String caption,
            List<MediaItemData> media,
            String visibility,
            long likeCount,
            long replyCount,
            boolean likedByMe,
            List<String> hashtags,
            List<ProductTagData> productTags,
            boolean allowComments,
            String createdAt,
            String updatedAt
    ) {
    }

    public record ProductTagData(
            String productId,
            BigDecimal price
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
