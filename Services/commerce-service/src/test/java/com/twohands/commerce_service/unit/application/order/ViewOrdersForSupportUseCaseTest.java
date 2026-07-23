package com.twohands.commerce_service.unit.application.order;

import com.twohands.commerce_service.application.order.viewordersforsupport.ViewOrdersForSupportQuery;
import com.twohands.commerce_service.application.order.viewordersforsupport.ViewOrdersForSupportResult;
import com.twohands.commerce_service.application.order.viewordersforsupport.ViewOrdersForSupportUseCase;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.order.OrderSupportListEntry;
import com.twohands.commerce_service.domain.order.OrderSupportListPagedResult;
import com.twohands.commerce_service.domain.order.OrderSupportListSearchCriteria;
import com.twohands.commerce_service.domain.order.OrderSupportListSortField;
import com.twohands.commerce_service.domain.order.ViewOrdersForSupportRepository;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.support.WebhookSupportPageRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ViewOrdersForSupportUseCaseTest {

	private final ViewOrdersForSupportRepository repository = mock(ViewOrdersForSupportRepository.class);
	private final ViewOrdersForSupportUseCase useCase = new ViewOrdersForSupportUseCase(repository);

	@Test
	void execute_returnsPagedOrders() {
		OrderSupportListEntry entry = new OrderSupportListEntry(
				UUID.randomUUID(),
				UUID.randomUUID(),
				OrderStatus.PROCESSING,
				PaymentStatus.PAID,
				PaymentMethod.PAYOS,
				new BigDecimal("350000"),
				Instant.parse("2026-05-19T10:00:00Z"),
				Instant.parse("2026-05-20T08:00:00Z")
		);
		when(repository.search(any(OrderSupportListSearchCriteria.class), eq(new WebhookSupportPageRequest(1, 20))))
				.thenReturn(new OrderSupportListPagedResult(List.of(entry), 1, 20, 1L, 1));

		ViewOrdersForSupportResult result = useCase.execute(new ViewOrdersForSupportQuery(
				"PROCESSING",
				"PAYOS",
				"PAID",
				"f1000000",
				null,
				null,
				"created_at",
				1,
				20
		));

		assertEquals(1, result.orders().size());
		assertEquals(PaymentMethod.PAYOS, result.orders().getFirst().paymentMethod());
		verify(repository).search(
				eq(new OrderSupportListSearchCriteria(
						Optional.of(OrderStatus.PROCESSING),
						Optional.of(PaymentMethod.PAYOS),
						Optional.of(PaymentStatus.PAID),
						Optional.of("f1000000"),
						null,
						null,
						OrderSupportListSortField.CREATED_AT
				)),
				eq(new WebhookSupportPageRequest(1, 20))
		);
	}
}
