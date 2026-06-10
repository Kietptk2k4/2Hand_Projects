package com.twohands.commerce_service.unit.application.finance;

import com.twohands.commerce_service.application.finance.recordsellerledgercredit.RecordSellerLedgerCreditService;
import com.twohands.commerce_service.config.CommerceFinanceProperties;
import com.twohands.commerce_service.domain.finance.OrderItemLedgerSnapshot;
import com.twohands.commerce_service.domain.finance.SellerLedgerCreditDraft;
import com.twohands.commerce_service.domain.finance.SellerLedgerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecordSellerLedgerCreditServiceTest {

    @Mock
    private SellerLedgerRepository sellerLedgerRepository;

    @InjectMocks
    private RecordSellerLedgerCreditService service;

    @Test
    void shouldRecordCreditWithConfiguredCommissionRate() {
        CommerceFinanceProperties properties = new CommerceFinanceProperties();
        properties.setPlatformCommissionRate(new BigDecimal("0.10"));
        service = new RecordSellerLedgerCreditService(sellerLedgerRepository, properties);

        UUID orderItemId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        when(sellerLedgerRepository.findEligibleCreditSnapshots(List.of(orderItemId)))
                .thenReturn(List.of(new OrderItemLedgerSnapshot(orderItemId, sellerId, BigDecimal.valueOf(1_000_000))));
        when(sellerLedgerRepository.insertCreditIfAbsent(any())).thenReturn(true);

        int recorded = service.recordCreditsForCompletedOrderItems(
                List.of(orderItemId),
                Instant.parse("2026-06-10T10:00:00Z")
        );

        assertThat(recorded).isEqualTo(1);

        ArgumentCaptor<SellerLedgerCreditDraft> captor = ArgumentCaptor.forClass(SellerLedgerCreditDraft.class);
        verify(sellerLedgerRepository).insertCreditIfAbsent(captor.capture());
        assertThat(captor.getValue().amounts().platformFeeAmount()).isEqualByComparingTo(BigDecimal.valueOf(100_000));
        assertThat(captor.getValue().amounts().netAmount()).isEqualByComparingTo(BigDecimal.valueOf(900_000));
    }
}
