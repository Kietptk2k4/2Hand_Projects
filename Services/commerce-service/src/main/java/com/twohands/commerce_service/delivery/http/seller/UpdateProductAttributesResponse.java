package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.product.ProductStatus;

import java.util.List;
import java.util.UUID;

public record UpdateProductAttributesResponse(
        @JsonProperty("product_id") UUID productId,
        @JsonProperty("seller_id") UUID sellerId,
        @JsonProperty("shop_id") UUID shopId,
        ProductStatus status,
        List<AttributeItemResponse> attributes
) {

    public record AttributeItemResponse(
            @JsonProperty("attribute_name") String attributeName,
            @JsonProperty("attribute_value") String attributeValue
    ) {
    }
}
