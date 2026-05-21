package com.twohands.commerce_service.application.shipping;

import java.util.UUID;

public record SellerWeightGroup(
        UUID sellerId,
        UUID shopId,
        int totalWeightGram
) {
}
