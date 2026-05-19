package com.twohands.social_service.application.post.editpost;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record EditPostCommand(
        UUID editorId,
        String postId,
        Optional<String> caption,
        Optional<List<MediaItemCommand>> media,
        Optional<List<ProductTagCommand>> productTags,
        Optional<String> visibility,
        Optional<Boolean> allowComments,
        Optional<List<String>> hashtags
) {
    public record MediaItemCommand(String url, String type) {
    }

    public record ProductTagCommand(String productId, BigDecimal price) {
    }
}
