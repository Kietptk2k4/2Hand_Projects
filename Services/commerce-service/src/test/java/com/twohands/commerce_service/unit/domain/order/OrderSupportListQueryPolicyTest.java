package com.twohands.commerce_service.unit.domain.order;

import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.order.OrderSupportListQueryPolicy;
import com.twohands.commerce_service.domain.order.OrderSupportListSortField;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderSupportListQueryPolicyTest {

	@Test
	void parseStatus_acceptsValidStatusAndBlank() {
		assertTrue(OrderSupportListQueryPolicy.parseStatus(null).isEmpty());
		assertEquals(Optional.of(OrderStatus.PROCESSING), OrderSupportListQueryPolicy.parseStatus("processing"));
	}

	@Test
	void parseStatus_rejectsUnknownStatus() {
		AppException ex = assertThrows(AppException.class, () -> OrderSupportListQueryPolicy.parseStatus("UNKNOWN"));
		assertEquals(ErrorCode.VALIDATION_ERROR, ex.getErrorCode());
	}

	@Test
	void parsePaymentMethod_acceptsValidMethodAndBlank() {
		assertTrue(OrderSupportListQueryPolicy.parsePaymentMethod(null).isEmpty());
		assertEquals(Optional.of(PaymentMethod.COD), OrderSupportListQueryPolicy.parsePaymentMethod("cod"));
	}

	@Test
	void parseSortField_defaultsToCreatedAt() {
		assertEquals(OrderSupportListSortField.CREATED_AT, OrderSupportListQueryPolicy.parseSortField(null));
		assertEquals(OrderSupportListSortField.UPDATED_AT, OrderSupportListQueryPolicy.parseSortField("updated_at"));
	}

	@Test
	void parseSortField_rejectsUnknownSort() {
		AppException ex = assertThrows(AppException.class, () -> OrderSupportListQueryPolicy.parseSortField("completed_at"));
		assertEquals(ErrorCode.VALIDATION_ERROR, ex.getErrorCode());
	}
}
