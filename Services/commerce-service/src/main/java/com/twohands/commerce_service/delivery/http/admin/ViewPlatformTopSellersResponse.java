package com.twohands.commerce_service.delivery.http.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.finance.PlatformTopSeller;

import java.util.List;

public record ViewPlatformTopSellersResponse(
        @JsonProperty("items") List<PlatformTopSellerResponse> items
) {
    public static ViewPlatformTopSellersResponse from(List<PlatformTopSeller> sellers) {
        return new ViewPlatformTopSellersResponse(sellers.stream().map(PlatformTopSellerResponse::from).toList());
    }
}
