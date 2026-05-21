package com.twohands.commerce_service.unit.application.order;

import com.twohands.commerce_service.application.order.autocancelunpaidorder.AutoCancelUnpaidOrdersResult;
import com.twohands.commerce_service.application.order.autocancelunpaidorder.AutoCancelUnpaidOrdersUseCase;
import com.twohands.commerce_service.domain.order.ExpiredUnpaidOrderCandidate;
import com.twohands.commerce_service.domain.order.UnpaidOrderCancelOutcome;
import com.twohands.commerce_service.domain.order.UnpaidOrderCancellationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutoCancelUnpaidOrdersUseCaseTest {

    @Mock
    private UnpaidOrderCancellationRepository unpaidOrderCancellationRepository;

    private AutoCancelUnpaidOrdersUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AutoCancelUnpaidOrdersUseCase(unpaidOrderCancellationRepository, 10, 30);
    }

    @Test
    void shouldReturnZeroWhenNoCandidates() {
        when(unpaidOrderCancellationRepository.findExpiredCandidates(eq(10), any(Instant.class), any(Instant.class)))
                .thenReturn(List.of());

        AutoCancelUnpaidOrdersResult result = useCase.execute();

        assertThat(result.candidatesFound()).isZero();
        assertThat(result.cancelled()).isZero();
        verify(unpaidOrderCancellationRepository, never()).cancelExpiredUnpaidOrder(any(), any(), any());
    }

    @Test
    void shouldCancelExpiredCandidates() {
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        when(unpaidOrderCancellationRepository.findExpiredCandidates(eq(10), any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(new ExpiredUnpaidOrderCandidate(orderId, paymentId)));
        when(unpaidOrderCancellationRepository.cancelExpiredUnpaidOrder(eq(orderId), eq(paymentId), any(Instant.class)))
                .thenReturn(UnpaidOrderCancelOutcome.CANCELLED);

        AutoCancelUnpaidOrdersResult result = useCase.execute();

        assertThat(result.candidatesFound()).isEqualTo(1);
        assertThat(result.cancelled()).isEqualTo(1);
        assertThat(result.skipped()).isZero();
        assertThat(result.failed()).isZero();
    }

    @Test
    void shouldCountSkippedAndFailedOutcomes() {
        UUID orderId1 = UUID.randomUUID();
        UUID paymentId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();
        UUID paymentId2 = UUID.randomUUID();

        when(unpaidOrderCancellationRepository.findExpiredCandidates(eq(10), any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(
                        new ExpiredUnpaidOrderCandidate(orderId1, paymentId1),
                        new ExpiredUnpaidOrderCandidate(orderId2, paymentId2)
                ));
        when(unpaidOrderCancellationRepository.cancelExpiredUnpaidOrder(eq(orderId1), eq(paymentId1), any(Instant.class)))
                .thenReturn(UnpaidOrderCancelOutcome.SKIPPED_PAYMENT_NOT_PENDING);
        when(unpaidOrderCancellationRepository.cancelExpiredUnpaidOrder(eq(orderId2), eq(paymentId2), any(Instant.class)))
                .thenThrow(new RuntimeException("db error"));

        AutoCancelUnpaidOrdersResult result = useCase.execute();

        assertThat(result.candidatesFound()).isEqualTo(2);
        assertThat(result.cancelled()).isZero();
        assertThat(result.skipped()).isEqualTo(1);
        assertThat(result.failed()).isEqualTo(1);
    }
}
