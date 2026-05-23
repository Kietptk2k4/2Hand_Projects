package com.twohands.commerce_service.unit.application.payment;

import com.twohands.commerce_service.application.payment.viewpaymentsupport.ViewPaymentSupportDetailCommand;
import com.twohands.commerce_service.application.payment.viewpaymentsupport.ViewPaymentSupportDetailUseCase;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.payment.PaymentSupportDetailSnapshot;
import com.twohands.commerce_service.domain.payment.ViewPaymentSupportDetailRepository;
import com.twohands.commerce_service.domain.payment.ViewPaymentSupportDetailResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ViewPaymentSupportDetailUseCaseTest {

	private final ViewPaymentSupportDetailRepository repository = mock(ViewPaymentSupportDetailRepository.class);
	private final Clock clock = Clock.fixed(Instant.parse("2026-05-20T12:00:00Z"), ZoneOffset.UTC);
	private final ViewPaymentSupportDetailUseCase useCase = new ViewPaymentSupportDetailUseCase(repository, clock);

	@Test
	void execute_returnsSupportDetailWithoutCheckoutUrl() {
		UUID paymentId = UUID.randomUUID();
		PaymentSupportDetailSnapshot snapshot = new PaymentSupportDetailSnapshot(
				paymentId,
				UUID.randomUUID(),
				UUID.randomUUID(),
				PaymentMethod.PAYOS,
				BigDecimal.TEN,
				"VND",
				PaymentStatus.PENDING,
				null,
				null,
				Instant.parse("2026-05-19T00:00:00Z"),
				Instant.parse("2026-05-20T00:00:00Z"),
				"PAYOS-001",
				null,
				"https://payos.example/checkout",
				Instant.parse("2026-05-21T00:00:00Z"),
				OrderStatus.AWAITING_PAYMENT,
				PaymentStatus.PENDING,
				List.of(),
				List.of()
		);
		when(repository.findByPaymentId(paymentId)).thenReturn(Optional.of(snapshot));

		ViewPaymentSupportDetailResult result = useCase.execute(new ViewPaymentSupportDetailCommand(paymentId));

		assertEquals(paymentId, result.paymentId());
		assertTrue(result.checkoutUrlAvailable());
		assertEquals("AWAITING_WEBHOOK", result.reconciliationStatus());
	}

	@Test
	void execute_throwsWhenPaymentNotFound() {
		UUID paymentId = UUID.randomUUID();
		when(repository.findByPaymentId(paymentId)).thenReturn(Optional.empty());

		AppException ex = assertThrows(AppException.class, () -> useCase.execute(new ViewPaymentSupportDetailCommand(paymentId)));

		assertEquals(ErrorCode.PAYMENT_NOT_FOUND, ex.getErrorCode());
	}
}
