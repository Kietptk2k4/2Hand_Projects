package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.delivery.http.catalog.PageMetaResponse;
import com.twohands.commerce_service.domain.finance.ViewSellerLedgerResult;

import java.util.List;

public record ViewSellerLedgerResponse(
        @JsonProperty("items") List<SellerLedgerEntryResponse> items,
        @JsonProperty("pagination") PageMetaResponse pagination
) {
    public static ViewSellerLedgerResponse from(ViewSellerLedgerResult result) {
        PageMeta pagination = result.pagination();
        return new ViewSellerLedgerResponse(
                result.items().stream().map(SellerLedgerEntryResponse::from).toList(),
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
