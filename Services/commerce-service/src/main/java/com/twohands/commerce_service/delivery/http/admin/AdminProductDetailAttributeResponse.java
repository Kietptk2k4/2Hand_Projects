package com.twohands.commerce_service.delivery.http.admin;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AdminProductDetailAttributeResponse(
        @JsonProperty("attribute_name") String attributeName,
        @JsonProperty("attribute_value") String attributeValue
) {
}
