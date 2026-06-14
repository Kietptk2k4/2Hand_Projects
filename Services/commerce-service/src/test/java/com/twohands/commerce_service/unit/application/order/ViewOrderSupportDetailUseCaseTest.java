package com.twohands.commerce_service.unit.application.order;

import com.twohands.commerce_service.application.order.viewordersupport.ViewOrderSupportDetailCommand;
import com.twohands.commerce_service.application.order.viewordersupport.ViewOrderSupportDetailUseCase;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.order.ViewOrderDetailRepository;
import com.twohands.commerce_service.domain.order.ViewOrderDetailResult;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ViewOrderSupportDetailUseCaseTest {

	private final ViewOrderDetailRepository repository = mock(ViewOrderDetailRepository.class);
	private final ViewOrderSupportDetailUseCase useCase = new ViewOrderSupportDetailUseCase(repository);

	@Test
	void execute_returnsOrderWhenFound() {
		UUID orderId = UUID.randomUUID();
		ViewOrderDetailResult expected = new ViewOrderDetailResult(
				orderId,
				UUID.randomUUID(),
				OrderStatus.PROCESSING,
				PaymentStatus.PENDING,
				PaymentMethod.COD,
				BigDecimal.TEN,
				BigDecimal.TEN,
				Instant.now(),
				Instant.now(),
				null,
				null,
				List.of(),
                List.of(),
                List.of(),
				null
		);
		when(repository.findByOrderId(orderId)).thenReturn(Optional.of(expected));

		ViewOrderDetailResult result = useCase.execute(new ViewOrderSupportDetailCommand(orderId));

		assertEquals(orderId, result.orderId());
	}

	@Test
	void execute_throwsWhenOrderNotFound() {
		UUID orderId = UUID.randomUUID();
		when(repository.findByOrderId(orderId)).thenReturn(Optional.empty());

		AppException ex = assertThrows(AppException.class, () -> useCase.execute(new ViewOrderSupportDetailCommand(orderId)));

		assertEquals(ErrorCode.ORDER_NOT_FOUND, ex.getErrorCode());
	}
}
