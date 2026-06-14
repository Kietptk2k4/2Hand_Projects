package com.twohands.commerce_service.domain.order;

import java.util.UUID;

public record CommerceBuyerSummary(
        UUID buyerId,
        String displayName,
        String avatarUrl
) {
    private static final String FALLBACK_DISPLAY_NAME = "Ng\u01b0\u1eddi mua";

    public static CommerceBuyerSummary empty() {
        return new CommerceBuyerSummary(null, FALLBACK_DISPLAY_NAME, null);
    }
}
