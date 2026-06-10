package com.twohands.commerce_service.unit.application.payment;

import com.twohands.commerce_service.application.payment.viewpaymentsupport.ViewPaymentsForSupportQuery;
import com.twohands.commerce_service.application.payment.viewpaymentsupport.ViewPaymentsForSupportResult;
import com.twohands.commerce_service.application.payment.viewpaymentsupport.ViewPaymentsForSupportUseCase;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.payment.PaymentSupportListEntry;
import com.twohands.commerce_service.domain.payment.PaymentSupportPagedResult;
import com.twohands.commerce_service.domain.payment.PaymentSupportSearchCriteria;
import com.twohands.commerce_service.domain.payment.ViewPaymentsForSupportRepository;
import com.twohands.commerce_service.domain.support.WebhookSupportPageRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ViewPaymentsForSupportUseCaseTest {

    private final ViewPaymentsForSupportRepository repository = mock(ViewPaymentsForSupportRepository.class);
    private final ViewPaymentsForSupportUseCase useCase = new ViewPaymentsForSupportUseCase(repository);

    @Test
    void execute_returnsPagedPaymentsSortedByCreatedAtDesc() {
        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PaymentSupportListEntry entry = new PaymentSupportListEntry(
                paymentId,
                orderId,
                PaymentMethod.COD,
                BigDecimal.valueOf(250000),
                "VND",
                PaymentStatus.PENDING,
                null,
                Instant.parse("2026-06-09T10:00:00Z")
        );
        when(repository.search(any(PaymentSupportSearchCriteria.class), eq(new WebhookSupportPageRequest(1, 20))))
                .thenReturn(new PaymentSupportPagedResult(List.of(entry), 1, 20, 1L, 1));

        ViewPaymentsForSupportResult result = useCase.execute(new ViewPaymentsForSupportQuery(
                null,
                "COD",
                null,
                null,
                null,
                1,
                20
        ));

        assertEquals(1, result.payments().size());
        assertEquals(PaymentMethod.COD, result.payments().getFirst().paymentMethod());
        verify(repository).search(any(PaymentSupportSearchCriteria.class), eq(new WebhookSupportPageRequest(1, 20)));
    }
}
