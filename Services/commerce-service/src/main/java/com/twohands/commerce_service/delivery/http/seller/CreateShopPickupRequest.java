package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateShopPickupRequest(
        @JsonProperty("pickup_name")
        String pickupName,

        String phone,

        @JsonProperty("province_code")
        String provinceCode,

        @JsonProperty("district_code")
        String districtCode,

        @JsonProperty("ward_code")
        String wardCode,

        @JsonProperty("address_detail")
        String addressDetail
) {
    public boolean hasAnyField() {
        return isPresent(pickupName)
                || isPresent(phone)
                || isPresent(provinceCode)
                || isPresent(districtCode)
                || isPresent(wardCode)
                || isPresent(addressDetail);
    }

    private boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }
}
