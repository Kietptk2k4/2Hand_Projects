package com.twohands.commerce_service.delivery.http.address;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

public record UpdateUserAddressRequest(
        @JsonProperty("receiver_name")
        @Size(max = 255, message = "receiver_name must be at most 255 characters")
        String receiverName,

        @Size(max = 50, message = "phone must be at most 50 characters")
        String phone,

        @JsonProperty("province_code")
        @Size(max = 50, message = "province_code must be at most 50 characters")
        String provinceCode,

        @JsonProperty("district_code")
        @Size(max = 50, message = "district_code must be at most 50 characters")
        String districtCode,

        @JsonProperty("ward_code")
        @Size(max = 50, message = "ward_code must be at most 50 characters")
        String wardCode,

        @JsonProperty("address_detail")
        String addressDetail,

        @JsonProperty("is_default")
        Boolean isDefault
) {
}
