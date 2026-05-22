package com.twohands.commerce_service.delivery.http.address;

import java.util.List;

public record ViewUserAddressesResponse(
        List<UserAddressItemResponse> addresses
) {
}
