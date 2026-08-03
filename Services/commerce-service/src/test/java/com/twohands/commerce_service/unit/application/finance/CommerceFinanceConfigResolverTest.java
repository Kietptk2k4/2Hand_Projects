package com.twohands.commerce_service.unit.application.finance;

import com.twohands.commerce_service.application.finance.CommerceFinanceConfigResolver;
import com.twohands.commerce_service.config.CommerceFinanceProperties;
import com.twohands.commerce_service.domain.integration.AdminSystemConfigClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommerceFinanceConfigResolverTest {

    @Mock
    private AdminSystemConfigClient adminSystemConfigClient;

    private CommerceFinanceProperties financeProperties;
    private CommerceFinanceConfigResolver resolver;

    @BeforeEach
    void setUp() {
        financeProperties = new CommerceFinanceProperties();
        financeProperties.setPlatformCommissionRate(new BigDecimal("0.10"));
        financeProperties.setMinPayoutAmount(new BigDecimal("100000"));
        resolver = new CommerceFinanceConfigResolver(adminSystemConfigClient, financeProperties);
    }

    @Test
    void shouldUseAdminCommissionRateWhenPresent() {
        when(adminSystemConfigClient.findActiveConfigValue(
                CommerceFinanceConfigResolver.KEY_PLATFORM_COMMISSION_RATE
        )).thenReturn(Optional.of("0.15"));

        assertThat(resolver.resolvePlatformCommissionRate()).isEqualByComparingTo("0.15");
    }

    @Test
    void shouldFallbackCommissionWhenAdminMissing() {
        when(adminSystemConfigClient.findActiveConfigValue(
                CommerceFinanceConfigResolver.KEY_PLATFORM_COMMISSION_RATE
        )).thenReturn(Optional.empty());

        assertThat(resolver.resolvePlatformCommissionRate()).isEqualByComparingTo("0.10");
    }

    @Test
    void shouldFallbackCommissionWhenAdminValueInvalid() {
        when(adminSystemConfigClient.findActiveConfigValue(
                CommerceFinanceConfigResolver.KEY_PLATFORM_COMMISSION_RATE
        )).thenReturn(Optional.of("1.5"));

        assertThat(resolver.resolvePlatformCommissionRate()).isEqualByComparingTo("0.10");
    }

    @Test
    void shouldUseAdminMinPayoutWhenPresent() {
        when(adminSystemConfigClient.findActiveConfigValue(
                CommerceFinanceConfigResolver.KEY_MIN_PAYOUT_AMOUNT
        )).thenReturn(Optional.of("200000"));

        assertThat(resolver.resolveMinPayoutAmount()).isEqualByComparingTo("200000");
    }

    @Test
    void shouldFallbackMinPayoutWhenAdminValueInvalid() {
        when(adminSystemConfigClient.findActiveConfigValue(
                CommerceFinanceConfigResolver.KEY_MIN_PAYOUT_AMOUNT
        )).thenReturn(Optional.of("0"));

        assertThat(resolver.resolveMinPayoutAmount()).isEqualByComparingTo("100000");
    }
}
