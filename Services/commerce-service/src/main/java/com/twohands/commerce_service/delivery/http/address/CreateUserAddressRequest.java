package com.twohands.commerce_service.delivery.http.address;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserAddressRequest(
        @JsonProperty("receiver_name")
        @NotBlank(message = "receiver_name is required")
        @Size(max = 255, message = "receiver_name must be at most 255 characters")
        String receiverName,

        @NotBlank(message = "phone is required")
        @Size(max = 50, message = "phone must be at most 50 characters")
        String phone,

        @JsonProperty("province_code")
        @NotBlank(message = "province_code is required")
        @Size(max = 50, message = "province_code must be at most 50 characters")
        String provinceCode,

        @JsonProperty("district_code")
        @NotBlank(message = "district_code is required")
        @Size(max = 50, message = "district_code must be at most 50 characters")
        String districtCode,

        @JsonProperty("ward_code")
        @NotBlank(message = "ward_code is required")
        @Size(max = 50, message = "ward_code must be at most 50 characters")
        String wardCode,

        @JsonProperty("address_detail")
        @NotBlank(message = "address_detail is required")
        String addressDetail,

        @JsonProperty("is_default")
        Boolean isDefault
) {
}
