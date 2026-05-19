package com.twohands.social_service.domain.post;

import java.util.List;

public record PostHashtagSearchQuery(
        List<String> hashtagVariants,
        int page,
        int size
) {
}
