package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.delivery.http.catalog.PageMetaResponse;
import com.twohands.commerce_service.domain.finance.ViewSellerPayoutRequestsResult;

import java.util.List;

public record ViewSellerPayoutRequestsResponse(
        @JsonProperty("items") List<SellerPayoutRequestResponse> items,
        @JsonProperty("pagination") PageMetaResponse pagination
) {
    public static ViewSellerPayoutRequestsResponse from(ViewSellerPayoutRequestsResult result) {
        PageMeta pagination = result.pagination();
        return new ViewSellerPayoutRequestsResponse(
                result.items().stream().map(SellerPayoutRequestResponse::from).toList(),
                new PageMetaResponse(
                        pagination.page(),
                        pagination.limit(),
                        pagination.totalItems(),
                        pagination.totalPages(),
                        pagination.hasNext()
                )
        );
    }
}
