package com.twohands.commerce_service.delivery.http.address;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record CreateUserAddressResponse(
        @JsonProperty("address_id") UUID addressId,
        @JsonProperty("user_id") UUID userId,
        @JsonProperty("receiver_name") String receiverName,
        String phone,
        @JsonProperty("province_code") String provinceCode,
        @JsonProperty("district_code") String districtCode,
        @JsonProperty("ward_code") String wardCode,
        @JsonProperty("address_detail") String addressDetail,
        @JsonProperty("is_default") boolean isDefault,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt
) {
}
