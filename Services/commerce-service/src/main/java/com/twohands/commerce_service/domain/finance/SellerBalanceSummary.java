package com.twohands.commerce_service.domain.finance;



import java.math.BigDecimal;



public record SellerBalanceSummary(

        BigDecimal availableBalance,

        BigDecimal totalPlatformFee,

        BigDecimal totalNetCredited,

        BigDecimal pendingPayoutAmount,

        long creditEntryCount

) {

    public static SellerBalanceSummary empty() {

        return new SellerBalanceSummary(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0L);

    }

}

