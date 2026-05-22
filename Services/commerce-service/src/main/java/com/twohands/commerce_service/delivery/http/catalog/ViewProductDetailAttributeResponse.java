package com.twohands.commerce_service.delivery.http.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ViewProductDetailAttributeResponse(
        @JsonProperty("attribute_name") String attributeName,
        @JsonProperty("attribute_value") String attributeValue
) {
}
