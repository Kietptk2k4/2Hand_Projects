package com.twohands.social_service.delivery.http.post.mapper;

import com.twohands.social_service.application.post.common.ProductTagSnapshotData;
import com.twohands.social_service.delivery.http.post.response.CreatePostResponse;
import com.twohands.social_service.delivery.http.post.response.EditPostResponse;
import com.twohands.social_service.delivery.http.post.response.ViewPostDetailResponse;

public final class ProductTagHttpMapper {

    private ProductTagHttpMapper() {
    }

    public static CreatePostResponse.ProductTagResponse toCreateResponse(ProductTagSnapshotData tag) {
        return new CreatePostResponse.ProductTagResponse(
                tag.productId(),
                tag.price(),
                tag.name(),
                tag.imageUrl(),
                tag.category(),
                tag.available()
        );
    }

    public static EditPostResponse.ProductTagResponse toEditResponse(ProductTagSnapshotData tag) {
        return new EditPostResponse.ProductTagResponse(
                tag.productId(),
                tag.price(),
                tag.name(),
                tag.imageUrl(),
                tag.category(),
                tag.available()
        );
    }

    public static ViewPostDetailResponse.ProductTagResponse toDetailResponse(ProductTagSnapshotData tag) {
        return new ViewPostDetailResponse.ProductTagResponse(
                tag.productId(),
                tag.price(),
                tag.name(),
                tag.imageUrl(),
                tag.category(),
                tag.available()
        );
    }
}
