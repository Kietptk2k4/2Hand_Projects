package com.twohands.commerce_service.domain.finance;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class SellerLedgerCommission {

    private SellerLedgerCommission() {
    }

    public static SellerLedgerAmounts calculate(BigDecimal grossAmount, BigDecimal commissionRate) {
        if (grossAmount == null || grossAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("grossAmount must be >= 0");
        }
        if (commissionRate == null
                || commissionRate.compareTo(BigDecimal.ZERO) < 0
                || commissionRate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("commissionRate must be between 0 and 1");
        }

        BigDecimal platformFee = grossAmount
                .multiply(commissionRate)
                .setScale(0, RoundingMode.HALF_UP);
        BigDecimal netAmount = grossAmount.subtract(platformFee);
        return new SellerLedgerAmounts(grossAmount, platformFee, netAmount, commissionRate);
    }
}
