package com.twohands.commerce_service.unit.application.finance;

import com.twohands.commerce_service.application.finance.payout.createsellerpayoutrequest.CreateSellerPayoutRequestCommand;
import com.twohands.commerce_service.application.finance.payout.createsellerpayoutrequest.CreateSellerPayoutRequestUseCase;
import com.twohands.commerce_service.config.CommerceFinanceProperties;
import com.twohands.commerce_service.domain.finance.PayoutRequestStatus;
import com.twohands.commerce_service.domain.finance.SellerBalanceSummary;
import com.twohands.commerce_service.domain.finance.SellerLedgerRepository;
import com.twohands.commerce_service.domain.finance.SellerPayoutAccount;
import com.twohands.commerce_service.domain.finance.SellerPayoutRequest;
import com.twohands.commerce_service.domain.finance.SellerPayoutRepository;
import com.twohands.commerce_service.domain.shop.SellerShop;
import com.twohands.commerce_service.domain.shop.SellerShopRepository;
import com.twohands.commerce_service.domain.shop.ShopStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateSellerPayoutRequestUseCaseTest {

    @Mock
    private SellerShopRepository sellerShopRepository;

    @Mock
    private SellerPayoutRepository sellerPayoutRepository;

    @Mock
    private SellerLedgerRepository sellerLedgerRepository;

    private CommerceFinanceProperties financeProperties;

    @InjectMocks
    private CreateSellerPayoutRequestUseCase useCase;

    private final UUID sellerId = UUID.randomUUID();
    private final UUID accountId = UUID.randomUUID();
    private final UUID payoutRequestId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        financeProperties = new CommerceFinanceProperties();
        financeProperties.setMinPayoutAmount(new BigDecimal("100000"));
        useCase = new CreateSellerPayoutRequestUseCase(
                sellerShopRepository,
                sellerPayoutRepository,
                sellerLedgerRepository,
                financeProperties
        );
    }

    @Test
    void shouldCreatePayoutRequestWhenBalanceIsSufficient() {
        when(sellerShopRepository.findBySellerId(sellerId)).thenReturn(Optional.of(shop()));
        when(sellerPayoutRepository.findAccountById(sellerId, accountId)).thenReturn(Optional.of(account()));
        when(sellerLedgerRepository.findBalanceSummary(sellerId)).thenReturn(
                new SellerBalanceSummary(
                        new BigDecimal("500000"),
                        BigDecimal.ZERO,
                        new BigDecimal("500000"),
                        BigDecimal.ZERO,
                        1L
                )
        );
        when(sellerPayoutRepository.createPayoutRequest(eq(sellerId), eq(accountId), any(), any()))
                .thenReturn(payoutRequestId);
        when(sellerPayoutRepository.findPayoutRequestForSeller(sellerId, payoutRequestId))
                .thenReturn(Optional.of(payoutRequest()));

        SellerPayoutRequest result = useCase.execute(
                new CreateSellerPayoutRequestCommand(sellerId, accountId, new BigDecimal("200000"))
        );

        assertThat(result.amount()).isEqualByComparingTo("200000");
        verify(sellerPayoutRepository).createPayoutRequest(eq(sellerId), eq(accountId), eq(new BigDecimal("200000")), any());
    }

    @Test
    void shouldRejectAmountBelowMinimum() {
        when(sellerShopRepository.findBySellerId(sellerId)).thenReturn(Optional.of(shop()));
        when(sellerPayoutRepository.findAccountById(sellerId, accountId)).thenReturn(Optional.of(account()));

        assertThatThrownBy(() -> useCase.execute(
                new CreateSellerPayoutRequestCommand(sellerId, accountId, new BigDecimal("50000"))
        ))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.PAYOUT_AMOUNT_BELOW_MINIMUM);
    }

    @Test
    void shouldRejectWhenInsufficientBalance() {
        when(sellerShopRepository.findBySellerId(sellerId)).thenReturn(Optional.of(shop()));
        when(sellerPayoutRepository.findAccountById(sellerId, accountId)).thenReturn(Optional.of(account()));
        when(sellerLedgerRepository.findBalanceSummary(sellerId)).thenReturn(
                new SellerBalanceSummary(
                        new BigDecimal("100000"),
                        BigDecimal.ZERO,
                        new BigDecimal("100000"),
                        BigDecimal.ZERO,
                        1L
                )
        );

        assertThatThrownBy(() -> useCase.execute(
                new CreateSellerPayoutRequestCommand(sellerId, accountId, new BigDecimal("150000"))
        ))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INSUFFICIENT_PAYOUT_BALANCE);
    }

    private SellerShop shop() {
        return new SellerShop(UUID.randomUUID(), sellerId, ShopStatus.ACTIVE);
    }

    private SellerPayoutAccount account() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        return new SellerPayoutAccount(accountId, sellerId, "VCB", "Seller A", "123456", true, now, now);
    }

    private SellerPayoutRequest payoutRequest() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        return new SellerPayoutRequest(
                payoutRequestId,
                sellerId,
                accountId,
                new BigDecimal("200000"),
                PayoutRequestStatus.REQUESTED,
                null,
                null,
                now,
                null,
                null,
                null,
                null,
                "VCB",
                "Seller A",
                "123456"
        );
    }
}
