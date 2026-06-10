package com.twohands.commerce_service.delivery.http.seller;



import com.fasterxml.jackson.annotation.JsonProperty;

import com.twohands.commerce_service.domain.finance.SellerBalanceSummary;



import java.math.BigDecimal;



public record SellerBalanceSummaryResponse(

        @JsonProperty("available_balance") BigDecimal availableBalance,

        @JsonProperty("total_platform_fee") BigDecimal totalPlatformFee,

        @JsonProperty("total_net_credited") BigDecimal totalNetCredited,

        @JsonProperty("pending_payout_amount") BigDecimal pendingPayoutAmount,

        @JsonProperty("credit_entry_count") long creditEntryCount

) {

    public static SellerBalanceSummaryResponse from(SellerBalanceSummary balance) {

        return new SellerBalanceSummaryResponse(

                balance.availableBalance(),

                balance.totalPlatformFee(),

                balance.totalNetCredited(),

                balance.pendingPayoutAmount(),

                balance.creditEntryCount()

        );

    }

}

