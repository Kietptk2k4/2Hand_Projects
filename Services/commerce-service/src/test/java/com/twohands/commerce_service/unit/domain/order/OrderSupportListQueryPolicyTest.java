package com.twohands.commerce_service.unit.domain.order;

import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.order.OrderSupportListQueryPolicy;
import com.twohands.commerce_service.domain.order.OrderSupportListSortField;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
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
	void parsePaymentMethod_acceptsVnpay() {
		assertEquals(Optional.of(PaymentMethod.VNPAY), OrderSupportListQueryPolicy.parsePaymentMethod("vnpay"));
	}

	@Test
	void parsePaymentStatus_acceptsValidStatusAndBlank() {
		assertTrue(OrderSupportListQueryPolicy.parsePaymentStatus(null).isEmpty());
		assertEquals(Optional.of(PaymentStatus.PAID), OrderSupportListQueryPolicy.parsePaymentStatus("paid"));
	}

	@Test
	void parsePaymentStatus_rejectsUnknownStatus() {
		AppException ex = assertThrows(AppException.class, () -> OrderSupportListQueryPolicy.parsePaymentStatus("UNKNOWN"));
		assertEquals(ErrorCode.VALIDATION_ERROR, ex.getErrorCode());
	}

	@Test
	void parseSearchQuery_acceptsUuidFragmentAndRejectsInvalidCharacters() {
		assertTrue(OrderSupportListQueryPolicy.parseSearchQuery(null).isEmpty());
		assertEquals(Optional.of("f1000000"), OrderSupportListQueryPolicy.parseSearchQuery("f1000000"));
		AppException ex = assertThrows(AppException.class, () -> OrderSupportListQueryPolicy.parseSearchQuery("not-a-uuid"));
		assertEquals(ErrorCode.VALIDATION_ERROR, ex.getErrorCode());
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
