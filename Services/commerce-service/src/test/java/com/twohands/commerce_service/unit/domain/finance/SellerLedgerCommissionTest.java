package com.twohands.commerce_service.unit.domain.finance;

import com.twohands.commerce_service.domain.finance.SellerLedgerAmounts;
import com.twohands.commerce_service.domain.finance.SellerLedgerCommission;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SellerLedgerCommissionTest {

    @Test
    void shouldCalculateTenPercentCommissionInVnd() {
        SellerLedgerAmounts amounts = SellerLedgerCommission.calculate(
                BigDecimal.valueOf(1_000_000),
                new BigDecimal("0.10")
        );

        assertThat(amounts.grossAmount()).isEqualByComparingTo(BigDecimal.valueOf(1_000_000));
        assertThat(amounts.platformFeeAmount()).isEqualByComparingTo(BigDecimal.valueOf(100_000));
        assertThat(amounts.netAmount()).isEqualByComparingTo(BigDecimal.valueOf(900_000));
    }

    @Test
    void shouldRoundPlatformFeeHalfUp() {
        SellerLedgerAmounts amounts = SellerLedgerCommission.calculate(
                BigDecimal.valueOf(890_000),
                new BigDecimal("0.10")
        );

        assertThat(amounts.platformFeeAmount()).isEqualByComparingTo(BigDecimal.valueOf(89_000));
        assertThat(amounts.netAmount()).isEqualByComparingTo(BigDecimal.valueOf(801_000));
    }
}
