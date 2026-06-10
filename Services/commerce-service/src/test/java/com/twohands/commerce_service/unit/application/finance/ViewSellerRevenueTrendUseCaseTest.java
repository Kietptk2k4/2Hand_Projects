package com.twohands.commerce_service.unit.application.finance;

import com.twohands.commerce_service.application.finance.viewsellerrevenuetrend.ViewSellerRevenueTrendCommand;
import com.twohands.commerce_service.application.finance.viewsellerrevenuetrend.ViewSellerRevenueTrendUseCase;
import com.twohands.commerce_service.domain.finance.RevenueTrendGranularity;
import com.twohands.commerce_service.domain.finance.SellerFinanceReadRepository;
import com.twohands.commerce_service.domain.finance.SellerRevenueTrendPoint;
import com.twohands.commerce_service.domain.finance.SellerRevenueTrendResult;
import com.twohands.commerce_service.domain.shop.SellerShop;
import com.twohands.commerce_service.domain.shop.SellerShopRepository;
import com.twohands.commerce_service.domain.shop.ShopStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViewSellerRevenueTrendUseCaseTest {

    @Mock
    private SellerShopRepository sellerShopRepository;

    @Mock
    private SellerFinanceReadRepository sellerFinanceReadRepository;

    private final Clock clock = Clock.fixed(Instant.parse("2026-06-10T12:00:00Z"), ZoneOffset.UTC);

    @InjectMocks
    private ViewSellerRevenueTrendUseCase useCase;

    private final UUID sellerId = UUID.randomUUID();

    @Test
    void shouldReturnDefaultTrendRange() {
        useCase = new ViewSellerRevenueTrendUseCase(sellerShopRepository, sellerFinanceReadRepository, clock);
        when(sellerShopRepository.findBySellerId(sellerId)).thenReturn(Optional.of(mockShop()));
        when(sellerFinanceReadRepository.findRecognizedRevenueTrend(
                eq(sellerId),
                any(),
                any(),
                eq(RevenueTrendGranularity.DAY)
        )).thenReturn(List.of(
                new SellerRevenueTrendPoint(Instant.parse("2026-06-09T00:00:00Z"), BigDecimal.valueOf(500_000), 1)
        ));

        SellerRevenueTrendResult result = useCase.execute(
                new ViewSellerRevenueTrendCommand(
                        sellerId,
                        Optional.empty(),
                        Optional.empty(),
                        RevenueTrendGranularity.DAY
                )
        );

        assertThat(result.points()).hasSize(1);
        assertThat(result.granularity()).isEqualTo(RevenueTrendGranularity.DAY);
    }

    @Test
    void shouldRejectInvalidGranularity() {
        assertThatThrownBy(() -> ViewSellerRevenueTrendUseCase.parseGranularity("YEAR"))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    private SellerShop mockShop() {
        return new SellerShop(UUID.randomUUID(), sellerId, ShopStatus.ACTIVE);
    }
}
