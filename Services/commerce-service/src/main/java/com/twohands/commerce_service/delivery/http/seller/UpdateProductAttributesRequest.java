package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateProductAttributesRequest(
        @NotNull(message = "attributes is required")
        @Valid
        List<AttributeItem> attributes
) {

    public record AttributeItem(
            @JsonProperty("attribute_name")
            @NotBlank(message = "attribute_name is required")
            @Size(max = 255, message = "attribute_name must be at most 255 characters")
            String attributeName,

            @JsonProperty("attribute_value")
            @NotBlank(message = "attribute_value is required")
            @Size(max = 500, message = "attribute_value must be at most 500 characters")
            String attributeValue
    ) {
    }
}
