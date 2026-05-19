package com.twohands.social_service.domain.post;

import java.math.BigDecimal;

public record ProductTag(
        String productId,
        BigDecimal price
) {
}
