package com.twohands.commerce_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "commerce.finance")
public class CommerceFinanceProperties {

    /**
     * Fallback platform commission on order_item.final_price (default 10%).
     * Prefer Admin system_config {@code COMMERCE_PLATFORM_COMMISSION_RATE} via CommerceFinanceConfigResolver.
     */
    private BigDecimal platformCommissionRate = new BigDecimal("0.10");

    /**
     * Fallback minimum payout amount in VND.
     * Prefer Admin system_config {@code COMMERCE_MIN_PAYOUT_AMOUNT} via CommerceFinanceConfigResolver.
     */
    private BigDecimal minPayoutAmount = new BigDecimal("100000");

    public BigDecimal getPlatformCommissionRate() {
        return platformCommissionRate;
    }

    public void setPlatformCommissionRate(BigDecimal platformCommissionRate) {
        this.platformCommissionRate = platformCommissionRate;
    }

    public BigDecimal getMinPayoutAmount() {
        return minPayoutAmount;
    }

    public void setMinPayoutAmount(BigDecimal minPayoutAmount) {
        this.minPayoutAmount = minPayoutAmount;
    }
}
