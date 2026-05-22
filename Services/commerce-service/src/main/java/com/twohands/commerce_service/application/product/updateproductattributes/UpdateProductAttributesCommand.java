package com.twohands.commerce_service.application.product.updateproductattributes;

import com.twohands.commerce_service.domain.product.ProductAttributeItem;

import java.util.List;
import java.util.UUID;

public record UpdateProductAttributesCommand(
        UUID sellerId,
        UUID productId,
        List<ProductAttributeItem> attributes
) {
}
