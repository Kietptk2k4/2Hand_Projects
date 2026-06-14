package com.twohands.commerce_service.unit.application.refund;

import com.twohands.commerce_service.application.refund.confirmrefundapproval.ConfirmRefundApprovalCommand;
import com.twohands.commerce_service.application.refund.confirmrefundapproval.ConfirmRefundApprovalUseCase;
import com.twohands.commerce_service.domain.order.AdminRefundApprovalItem;
import com.twohands.commerce_service.domain.order.AdminRefundApprovalRepository;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentRefundRequestStatus;
import com.twohands.commerce_service.domain.payment.PaymentRefundRequestedBy;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfirmRefundApprovalUseCaseTest {

    @Mock
    private AdminRefundApprovalRepository adminRefundApprovalRepository;

    @InjectMocks
    private ConfirmRefundApprovalUseCase useCase;

    private final UUID refundRequestId = UUID.randomUUID();

    @Test
    void shouldConfirmRefundApproval() {
        Instant now = Instant.parse("2026-06-13T10:00:00Z");
        AdminRefundApprovalItem confirmed = sampleItem(PaymentRefundRequestStatus.CONFIRMED, now);
        when(adminRefundApprovalRepository.confirmRefund(eq(refundRequestId), eq("manual refund"), any(Instant.class)))
                .thenReturn(confirmed);

        AdminRefundApprovalItem result = useCase.execute(
                new ConfirmRefundApprovalCommand(refundRequestId, "manual refund")
        );

        assertThat(result.status()).isEqualTo(PaymentRefundRequestStatus.CONFIRMED);
        assertThat(useCase.successMessage()).contains("hoan tien");
    }

    private AdminRefundApprovalItem sampleItem(PaymentRefundRequestStatus status, Instant confirmedAt) {
        return new AdminRefundApprovalItem(
                refundRequestId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                PaymentRefundRequestedBy.BUYER,
                UUID.randomUUID(),
                status,
                BigDecimal.valueOf(100_000),
                "BUYER_CANCELLED",
                "",
                PaymentMethod.VNPAY,
                PaymentStatus.REFUNDED,
                OrderStatus.CANCELLED,
                Instant.parse("2026-06-10T08:00:00Z"),
                confirmedAt,
                null
        );
    }
}
