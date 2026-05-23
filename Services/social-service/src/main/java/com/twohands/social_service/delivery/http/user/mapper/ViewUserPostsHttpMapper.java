package com.twohands.social_service.delivery.http.user.mapper;

import com.twohands.social_service.application.user.viewuserposts.ViewUserPostsResult;
import com.twohands.social_service.delivery.http.user.response.ViewUserPostsResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ViewUserPostsHttpMapper {

    public ViewUserPostsResponse toResponse(ViewUserPostsResult result) {
        List<ViewUserPostsResponse.UserPostItemResponse> items = result.items().stream()
                .map(this::toItem)
                .toList();
        ViewUserPostsResponse.PageMetaResponse meta = new ViewUserPostsResponse.PageMetaResponse(
                result.meta().page(),
                result.meta().size(),
                result.meta().totalElements(),
                result.meta().totalPages(),
                result.meta().hasNext()
        );
        return new ViewUserPostsResponse(items, meta);
    }

    private ViewUserPostsResponse.UserPostItemResponse toItem(ViewUserPostsResult.UserPostItem item) {
        List<ViewUserPostsResponse.MediaItemResponse> media = item.media().stream()
                .map(m -> new ViewUserPostsResponse.MediaItemResponse(m.url(), m.type()))
                .toList();
        return new ViewUserPostsResponse.UserPostItemResponse(
                item.postId(),
                item.caption(),
                media,
                item.visibility(),
                item.likeCount(),
                item.replyCount(),
                item.hashtags(),
                item.createdAt()
        );
    }
}
