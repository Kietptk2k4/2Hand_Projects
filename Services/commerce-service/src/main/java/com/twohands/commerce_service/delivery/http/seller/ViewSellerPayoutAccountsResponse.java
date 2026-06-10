package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.finance.ViewSellerPayoutAccountsResult;

import java.util.List;

public record ViewSellerPayoutAccountsResponse(
        @JsonProperty("accounts") List<SellerPayoutAccountResponse> accounts
) {
    public static ViewSellerPayoutAccountsResponse from(ViewSellerPayoutAccountsResult result) {
        return new ViewSellerPayoutAccountsResponse(
                result.accounts().stream().map(SellerPayoutAccountResponse::from).toList()
        );
    }
}
