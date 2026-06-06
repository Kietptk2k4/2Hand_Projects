package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SellerProductAttributeResponse(
        @JsonProperty("attribute_name") String attributeName,
        @JsonProperty("attribute_value") String attributeValue
) {
}
