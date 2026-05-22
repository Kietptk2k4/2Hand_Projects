package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ShippingAddressSnapshotResponse(
        @JsonProperty("receiver_name") String receiverName,
        String phone,
        @JsonProperty("province_code") String provinceCode,
        @JsonProperty("district_code") String districtCode,
        @JsonProperty("ward_code") String wardCode,
        @JsonProperty("address_detail") String addressDetail,
        @JsonProperty("full_address") String fullAddress
) {
}
