package com.twohands.commerce_service.application.product.updateproductmedia;

import java.util.List;
import java.util.UUID;

public record UpdateProductMediaCommand(
        UUID sellerId,
        UUID productId,
        List<String> mediaUrls
) {
}
