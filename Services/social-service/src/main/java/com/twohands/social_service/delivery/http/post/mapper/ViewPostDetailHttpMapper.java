package com.twohands.social_service.delivery.http.post.mapper;

import com.twohands.social_service.application.post.viewpostdetail.ViewPostDetailResult;
import com.twohands.social_service.delivery.http.post.response.ViewPostDetailResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ViewPostDetailHttpMapper {

    public ViewPostDetailResponse toResponse(ViewPostDetailResult result) {
        ViewPostDetailResponse.AuthorResponse author = new ViewPostDetailResponse.AuthorResponse(
                result.author().userId(),
                result.author().displayName(),
                result.author().avatarUrl()
        );
        List<ViewPostDetailResponse.MediaItemResponse> media = result.media().stream()
                .map(item -> new ViewPostDetailResponse.MediaItemResponse(item.url(), item.type(), item.width(), item.height()))
                .toList();
        List<ViewPostDetailResponse.ProductTagResponse> productTags = result.productTags().stream()
                .map(tag -> new ViewPostDetailResponse.ProductTagResponse(tag.productId(), tag.price()))
                .toList();

        return new ViewPostDetailResponse(
                result.postId(),
                author,
                result.caption(),
                media,
                productTags,
                result.visibility(),
                result.status(),
                result.likeCount(),
                result.replyCount(),
                result.hashtags(),
                result.allowComments(),
                result.likedByMe(),
                result.savedByMe(),
                result.isOwner(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
