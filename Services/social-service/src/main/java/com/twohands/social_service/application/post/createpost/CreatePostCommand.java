package com.twohands.social_service.application.post.createpost;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreatePostCommand(
        UUID authorId,
        String caption,
        List<MediaItemCommand> media,
        List<ProductTagCommand> productTags,
        String visibility,
        boolean allowComments,
        List<String> hashtags,
        boolean publish
) {
    public record MediaItemCommand(String url, String type) {
    }

    public record ProductTagCommand(String productId, BigDecimal price) {
    }
}
