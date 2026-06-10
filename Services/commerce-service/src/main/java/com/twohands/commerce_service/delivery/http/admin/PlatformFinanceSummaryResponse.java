package com.twohands.commerce_service.delivery.http.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.finance.PlatformFinanceSummary;

import java.math.BigDecimal;
import java.time.Instant;

public record PlatformFinanceSummaryResponse(
        @JsonProperty("recognized_gmv") BigDecimal recognizedGmv,
        @JsonProperty("recognized_item_count") long recognizedItemCount,
        @JsonProperty("total_platform_fee") BigDecimal totalPlatformFee,
        @JsonProperty("cod_pipeline_amount") BigDecimal codPipelineAmount,
        @JsonProperty("pending_payout_count") long pendingPayoutCount,
        @JsonProperty("pending_payout_amount") BigDecimal pendingPayoutAmount,
        @JsonProperty("paid_payout_amount") BigDecimal paidPayoutAmount,
        @JsonProperty("from") Instant from,
        @JsonProperty("to") Instant to
) {
    public static PlatformFinanceSummaryResponse from(PlatformFinanceSummary summary) {
        return new PlatformFinanceSummaryResponse(
                summary.recognizedGmv(),
                summary.recognizedItemCount(),
                summary.totalPlatformFee(),
                summary.codPipelineAmount(),
                summary.pendingPayoutCount(),
                summary.pendingPayoutAmount(),
                summary.paidPayoutAmount(),
                summary.from(),
                summary.to()
        );
    }
}
