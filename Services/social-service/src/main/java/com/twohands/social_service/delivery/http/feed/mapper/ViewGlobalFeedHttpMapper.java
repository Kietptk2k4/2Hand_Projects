package com.twohands.social_service.delivery.http.feed.mapper;

import com.twohands.social_service.application.feed.viewglobalfeed.ViewGlobalFeedResult;
import com.twohands.social_service.delivery.http.feed.response.ViewGlobalFeedResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ViewGlobalFeedHttpMapper {

    public ViewGlobalFeedResponse toResponse(ViewGlobalFeedResult result) {
        List<ViewGlobalFeedResponse.PostItemResponse> items = result.items()
                .stream()
                .map(this::toPostItem)
                .toList();
        ViewGlobalFeedResponse.PageMetaResponse meta = new ViewGlobalFeedResponse.PageMetaResponse(
                result.meta().page(),
                result.meta().size(),
                result.meta().totalElements(),
                result.meta().totalPages(),
                result.meta().hasNext()
        );
        return new ViewGlobalFeedResponse(items, meta);
    }

    private ViewGlobalFeedResponse.PostItemResponse toPostItem(ViewGlobalFeedResult.FeedPostItem item) {
        List<ViewGlobalFeedResponse.MediaItemResponse> media = item.media()
                .stream()
                .map(this::toMedia)
                .toList();
        return new ViewGlobalFeedResponse.PostItemResponse(
                item.postId(),
                item.authorId(),
                item.caption(),
                media,
                item.visibility(),
                item.likeCount(),
                item.replyCount(),
                item.likedByMe(),
                item.hashtags(),
                item.allowComments(),
                item.createdAt(),
                item.updatedAt()
        );
    }

    private ViewGlobalFeedResponse.MediaItemResponse toMedia(ViewGlobalFeedResult.MediaItemData media) {
        return new ViewGlobalFeedResponse.MediaItemResponse(media.url(), media.type(), media.width(), media.height());
    }
}
