package com.twohands.commerce_service.unit.application.finance;

import com.twohands.commerce_service.application.finance.viewsellerrevenuesummary.ViewSellerRevenueSummaryCommand;
import com.twohands.commerce_service.application.finance.viewsellerrevenuesummary.ViewSellerRevenueSummaryUseCase;
import com.twohands.commerce_service.domain.finance.SellerBalanceSummary;
import com.twohands.commerce_service.domain.finance.SellerFinanceReadRepository;
import com.twohands.commerce_service.domain.finance.SellerLedgerRepository;
import com.twohands.commerce_service.domain.finance.SellerRevenueBucket;
import com.twohands.commerce_service.domain.finance.SellerRevenueSummary;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViewSellerRevenueSummaryUseCaseTest {

    @Mock
    private SellerShopRepository sellerShopRepository;

    @Mock
    private SellerFinanceReadRepository sellerFinanceReadRepository;

    @Mock
    private SellerLedgerRepository sellerLedgerRepository;

    @InjectMocks
    private ViewSellerRevenueSummaryUseCase useCase;

    private final UUID sellerId = UUID.randomUUID();

    @Test
    void shouldReturnRevenueSummary() {
        when(sellerShopRepository.findBySellerId(sellerId)).thenReturn(Optional.of(shop()));
        SellerRevenueSummary summary = new SellerRevenueSummary(
                new SellerRevenueBucket(BigDecimal.valueOf(100_000), 1),
                new SellerRevenueBucket(BigDecimal.valueOf(200_000), 2),
                new SellerRevenueBucket(BigDecimal.valueOf(300_000), 3),
                BigDecimal.valueOf(600_000),
                SellerBalanceSummary.empty(),
                null,
                null
        );
        when(sellerFinanceReadRepository.findRevenueSummary(eq(sellerId), any(), any())).thenReturn(summary);
        when(sellerLedgerRepository.findBalanceSummary(sellerId)).thenReturn(
                new SellerBalanceSummary(
                        BigDecimal.valueOf(270_000),
                        BigDecimal.valueOf(30_000),
                        BigDecimal.valueOf(270_000),
                        BigDecimal.ZERO,
                        1L
                )
        );

        SellerRevenueSummary result = useCase.execute(
                new ViewSellerRevenueSummaryCommand(sellerId, Optional.empty(), Optional.empty())
        );

        assertThat(result.totalGross()).isEqualByComparingTo(BigDecimal.valueOf(600_000));
        assertThat(result.balance().availableBalance()).isEqualByComparingTo(BigDecimal.valueOf(270_000));
        verify(sellerFinanceReadRepository).findRevenueSummary(sellerId, Optional.empty(), Optional.empty());
    }

    @Test
    void shouldThrowWhenSellerHasNoShop() {
        when(sellerShopRepository.findBySellerId(sellerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(
                new ViewSellerRevenueSummaryCommand(sellerId, Optional.empty(), Optional.empty())
        ))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SELLER_SHOP_NOT_FOUND);
    }

    private SellerShop shop() {
        return new SellerShop(UUID.randomUUID(), sellerId, ShopStatus.ACTIVE);
    }
}
