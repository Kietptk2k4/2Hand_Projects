package com.twohands.commerce_service.unit.application.shipping;

import com.twohands.commerce_service.application.shipping.ShippingFeeQuoteService;
import com.twohands.commerce_service.application.shipping.ghn.ResolveGhnServiceUseCase;
import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import com.twohands.commerce_service.domain.shipment.GhnLeadtimeGateway;
import com.twohands.commerce_service.domain.shipment.GhnLeadtimeQuery;
import com.twohands.commerce_service.domain.shipment.GhnLeadtimeResult;
import com.twohands.commerce_service.domain.shipment.GhnResolvedService;
import com.twohands.commerce_service.domain.shipment.GhnShippingFeeGateway;
import com.twohands.commerce_service.domain.shipment.GhnShippingFeeQuery;
import com.twohands.commerce_service.domain.shipment.GhnShippingFeeResult;
import com.twohands.commerce_service.domain.shipping.SellerShippingProfile;
import com.twohands.commerce_service.domain.shipping.ShipmentType;
import com.twohands.commerce_service.domain.shipping.ShippingFeeRequest;
import com.twohands.commerce_service.domain.shipping.ShippingGroupFeeQuote;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.infrastructure.shipping.MockShippingFeeCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShippingFeeQuoteServiceTest {

    @Mock
    private MockShippingFeeCalculator mockShippingFeeCalculator;
    @Mock
    private GhnShippingFeeGateway ghnShippingFeeGateway;
    @Mock
    private GhnLeadtimeGateway ghnLeadtimeGateway;
    @Mock
    private ResolveGhnServiceUseCase resolveGhnServiceUseCase;

    private CommerceIntegrationProperties integrationProperties;
    private ShippingFeeQuoteService service;

    private final UUID shopId = UUID.randomUUID();
    private final UUID sellerId = UUID.randomUUID();
    private final Clock clock = Clock.fixed(Instant.parse("2026-06-07T10:00:00Z"), ZoneId.of("UTC"));

    @BeforeEach
    void setUp() {
        integrationProperties = new CommerceIntegrationProperties();
        service = new ShippingFeeQuoteService(
                mockShippingFeeCalculator,
                integrationProperties,
                ghnShippingFeeGateway,
                ghnLeadtimeGateway,
                resolveGhnServiceUseCase,
                clock
        );
    }

    @Test
    void shouldUseMockWhenGhnDisabled() {
        integrationProperties.getGhn().setEnabled(false);
        SellerShippingProfile profile = profile("1444", "20308");
        when(mockShippingFeeCalculator.calculate(any(ShippingFeeRequest.class)))
                .thenReturn(BigDecimal.valueOf(30_000));

        ShippingGroupFeeQuote quote = service.quoteGroup(
                profile,
                "79",
                "1442",
                "20309",
                1200,
                ShipmentType.STANDARD
        );

        assertThat(quote.shippingFee()).isEqualByComparingTo(BigDecimal.valueOf(30_000));
    }

    @Test
    void shouldQuoteViaGhnWhenEnabledAndConfigured() {
        enableGhn();
        SellerShippingProfile profile = profile("1444", "20308");
        when(resolveGhnServiceUseCase.resolveForRoute(1444, 1442))
                .thenReturn(new GhnResolvedService(53320, 2, "Chuan", true));
        when(ghnShippingFeeGateway.calculateFee(any(GhnShippingFeeQuery.class)))
                .thenReturn(new GhnShippingFeeResult(
                        BigDecimal.valueOf(36_300),
                        BigDecimal.valueOf(36_300),
                        "{}",
                        false
                ));
        when(ghnLeadtimeGateway.calculateLeadtime(any(GhnLeadtimeQuery.class)))
                .thenReturn(new GhnLeadtimeResult(LocalDate.of(2026, 6, 12), "{}"));

        ShippingGroupFeeQuote quote = service.quoteGroup(
                profile,
                "79",
                "1442",
                "20309",
                1200,
                ShipmentType.STANDARD
        );

        assertThat(quote.shippingFee()).isEqualByComparingTo(BigDecimal.valueOf(36_300));
        assertThat(quote.estimatedDeliveryDate()).isEqualTo(LocalDate.of(2026, 6, 12));

        ArgumentCaptor<GhnShippingFeeQuery> captor = ArgumentCaptor.forClass(GhnShippingFeeQuery.class);
        verify(ghnShippingFeeGateway).calculateFee(captor.capture());
        GhnShippingFeeQuery query = captor.getValue();
        assertThat(query.fromDistrictId()).isEqualTo(1444);
        assertThat(query.toDistrictId()).isEqualTo(1442);
        assertThat(query.fromWardCode()).isEqualTo("20308");
        assertThat(query.toWardCode()).isEqualTo("20309");
        assertThat(query.serviceId()).isEqualTo(53320);
        assertThat(query.weightGram()).isEqualTo(1200);
    }

    @Test
    void shouldFallbackToHeuristicEtaWhenLeadtimeFails() {
        enableGhn();
        SellerShippingProfile profile = profile("1444", "20308");
        when(resolveGhnServiceUseCase.resolveForRoute(1444, 1442))
                .thenReturn(new GhnResolvedService(53320, 2, "Chuan", true));
        when(ghnShippingFeeGateway.calculateFee(any(GhnShippingFeeQuery.class)))
                .thenReturn(new GhnShippingFeeResult(
                        BigDecimal.valueOf(36_300),
                        BigDecimal.valueOf(36_300),
                        "{}",
                        false
                ));
        when(ghnLeadtimeGateway.calculateLeadtime(any(GhnLeadtimeQuery.class)))
                .thenThrow(new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "leadtime down"));

        ShippingGroupFeeQuote quote = service.quoteGroup(
                profile,
                "79",
                "1442",
                "20309",
                1200,
                ShipmentType.STANDARD
        );

        assertThat(quote.shippingFee()).isEqualByComparingTo(BigDecimal.valueOf(36_300));
        assertThat(quote.estimatedDeliveryDate()).isEqualTo(LocalDate.of(2026, 6, 10));
    }

    @Test
    void shouldRejectNonGhnReadyAddress() {
        enableGhn();

        assertThatThrownBy(() -> service.quoteGroup(
                profile("Quan 10", "20308"),
                "79",
                "1442",
                "",
                1000,
                ShipmentType.STANDARD
        ))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.GHN_ADDRESS_NOT_READY);
    }

    @Test
    void shouldFallbackToMockWhenGhnProviderFails() {
        enableGhn();
        integrationProperties.getGhn().setMockFallbackEnabled(true);
        SellerShippingProfile profile = profile("1444", "20308");
        when(resolveGhnServiceUseCase.resolveForRoute(1444, 1442))
                .thenThrow(new AppException(ErrorCode.GHN_PROVIDER_UNAVAILABLE, "GHN down"));
        when(mockShippingFeeCalculator.calculate(any(ShippingFeeRequest.class)))
                .thenReturn(BigDecimal.valueOf(25_000));

        ShippingGroupFeeQuote quote = service.quoteGroup(
                profile,
                "79",
                "1442",
                "20309",
                1000,
                ShipmentType.STANDARD
        );

        assertThat(quote.shippingFee()).isEqualByComparingTo(BigDecimal.valueOf(25_000));
    }

    private void enableGhn() {
        integrationProperties.getGhn().setEnabled(true);
        integrationProperties.getGhn().setToken("token");
        integrationProperties.getGhn().setShopId("885");
        integrationProperties.getGhn().setMockFallbackEnabled(false);
    }

    private SellerShippingProfile profile(String districtCode, String wardCode) {
        return new SellerShippingProfile(shopId, sellerId, "79", districtCode, wardCode);
    }
}
