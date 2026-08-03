package com.twohands.commerce_service.application.finance;

import com.twohands.commerce_service.config.CommerceFinanceProperties;
import com.twohands.commerce_service.domain.integration.AdminSystemConfigClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CommerceFinanceConfigResolver {

    public static final String KEY_PLATFORM_COMMISSION_RATE = "COMMERCE_PLATFORM_COMMISSION_RATE";
    public static final String KEY_MIN_PAYOUT_AMOUNT = "COMMERCE_MIN_PAYOUT_AMOUNT";

    private static final Logger log = LoggerFactory.getLogger(CommerceFinanceConfigResolver.class);
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE = BigDecimal.ONE;

    private final AdminSystemConfigClient adminSystemConfigClient;
    private final CommerceFinanceProperties financeProperties;

    public CommerceFinanceConfigResolver(
            AdminSystemConfigClient adminSystemConfigClient,
            CommerceFinanceProperties financeProperties
    ) {
        this.adminSystemConfigClient = adminSystemConfigClient;
        this.financeProperties = financeProperties;
    }

    /**
     * Platform commission rate in [0, 1]. Prefers Admin system-configs; falls back to env/property.
     */
    public BigDecimal resolvePlatformCommissionRate() {
        return adminSystemConfigClient.findActiveConfigValue(KEY_PLATFORM_COMMISSION_RATE)
                .map(this::parseCommissionRate)
                .orElseGet(() -> {
                    log.warn(
                            "Admin config {} unavailable; using fallback platformCommissionRate={}",
                            KEY_PLATFORM_COMMISSION_RATE,
                            financeProperties.getPlatformCommissionRate()
                    );
                    return financeProperties.getPlatformCommissionRate();
                });
    }

    /**
     * Minimum payout amount (VND). Prefers Admin system-configs; falls back to env/property.
     */
    public BigDecimal resolveMinPayoutAmount() {
        return adminSystemConfigClient.findActiveConfigValue(KEY_MIN_PAYOUT_AMOUNT)
                .map(this::parseMinPayoutAmount)
                .orElseGet(() -> {
                    log.warn(
                            "Admin config {} unavailable; using fallback minPayoutAmount={}",
                            KEY_MIN_PAYOUT_AMOUNT,
                            financeProperties.getMinPayoutAmount()
                    );
                    return financeProperties.getMinPayoutAmount();
                });
    }

    private BigDecimal parseCommissionRate(String raw) {
        try {
            BigDecimal rate = new BigDecimal(raw.trim());
            if (rate.compareTo(ZERO) < 0 || rate.compareTo(ONE) > 0) {
                log.warn(
                        "Invalid commission rate {} for {}; using fallback {}",
                        raw,
                        KEY_PLATFORM_COMMISSION_RATE,
                        financeProperties.getPlatformCommissionRate()
                );
                return financeProperties.getPlatformCommissionRate();
            }
            return rate;
        } catch (NumberFormatException ex) {
            log.warn(
                    "Non-decimal commission rate {} for {}; using fallback {}",
                    raw,
                    KEY_PLATFORM_COMMISSION_RATE,
                    financeProperties.getPlatformCommissionRate()
            );
            return financeProperties.getPlatformCommissionRate();
        }
    }

    private BigDecimal parseMinPayoutAmount(String raw) {
        try {
            BigDecimal amount = new BigDecimal(raw.trim());
            if (amount.compareTo(ZERO) <= 0) {
                log.warn(
                        "Invalid min payout {} for {}; using fallback {}",
                        raw,
                        KEY_MIN_PAYOUT_AMOUNT,
                        financeProperties.getMinPayoutAmount()
                );
                return financeProperties.getMinPayoutAmount();
            }
            return amount;
        } catch (NumberFormatException ex) {
            log.warn(
                    "Non-decimal min payout {} for {}; using fallback {}",
                    raw,
                    KEY_MIN_PAYOUT_AMOUNT,
                    financeProperties.getMinPayoutAmount()
            );
            return financeProperties.getMinPayoutAmount();
        }
    }
}
