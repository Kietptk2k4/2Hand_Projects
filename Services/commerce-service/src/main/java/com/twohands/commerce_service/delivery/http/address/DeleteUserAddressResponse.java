package com.twohands.commerce_service.delivery.http.address;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record DeleteUserAddressResponse(
        @JsonProperty("address_id") UUID addressId,
        @JsonProperty("user_id") UUID userId,
        @JsonProperty("was_default") boolean wasDefault,
        @JsonProperty("new_default_address_id") UUID newDefaultAddressId,
        @JsonProperty("deleted_at") Instant deletedAt
) {
}
