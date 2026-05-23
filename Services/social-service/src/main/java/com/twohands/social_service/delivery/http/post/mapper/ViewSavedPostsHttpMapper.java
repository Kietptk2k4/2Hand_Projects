package com.twohands.social_service.delivery.http.post.mapper;

import com.twohands.social_service.application.post.viewsavedposts.ViewSavedPostsResult;
import com.twohands.social_service.delivery.http.post.response.ViewSavedPostsResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ViewSavedPostsHttpMapper {

    public ViewSavedPostsResponse toResponse(ViewSavedPostsResult result) {
        List<ViewSavedPostsResponse.SavedPostItemResponse> items = result.items()
                .stream()
                .map(this::toItem)
                .toList();
        ViewSavedPostsResponse.PageMetaResponse meta = new ViewSavedPostsResponse.PageMetaResponse(
                result.meta().page(),
                result.meta().size(),
                result.meta().totalElements(),
                result.meta().totalPages(),
                result.meta().hasNext()
        );
        return new ViewSavedPostsResponse(items, meta);
    }

    private ViewSavedPostsResponse.SavedPostItemResponse toItem(ViewSavedPostsResult.SavedPostItem item) {
        List<ViewSavedPostsResponse.MediaItemResponse> media = item.media()
                .stream()
                .map(m -> new ViewSavedPostsResponse.MediaItemResponse(m.url(), m.type()))
                .toList();
        return new ViewSavedPostsResponse.SavedPostItemResponse(
                item.postId(),
                item.authorId(),
                item.caption(),
                media,
                item.visibility(),
                item.likeCount(),
                item.replyCount(),
                item.hashtags(),
                item.allowComments(),
                item.savedAt(),
                item.createdAt(),
                item.updatedAt()
        );
    }
}
