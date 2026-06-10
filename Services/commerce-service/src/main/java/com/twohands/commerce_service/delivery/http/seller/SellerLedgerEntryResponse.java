package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.finance.SellerLedgerListEntry;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SellerLedgerEntryResponse(
        @JsonProperty("id") UUID id,
        @JsonProperty("order_item_id") UUID orderItemId,
        @JsonProperty("entry_type") String entryType,
        @JsonProperty("gross_amount") BigDecimal grossAmount,
        @JsonProperty("platform_fee_amount") BigDecimal platformFeeAmount,
        @JsonProperty("net_amount") BigDecimal netAmount,
        @JsonProperty("commission_rate_snapshot") BigDecimal commissionRateSnapshot,
        @JsonProperty("status") String status,
        @JsonProperty("created_at") Instant createdAt
) {
    public static SellerLedgerEntryResponse from(SellerLedgerListEntry entry) {
        return new SellerLedgerEntryResponse(
                entry.id(),
                entry.orderItemId(),
                entry.entryType().name(),
                entry.grossAmount(),
                entry.platformFeeAmount(),
                entry.netAmount(),
                entry.commissionRateSnapshot(),
                entry.status().name(),
                entry.createdAt()
        );
    }
}
