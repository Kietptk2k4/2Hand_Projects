package com.twohands.commerce_service.unit.domain.payment;

import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.payment.PaymentSupportReconciliationPolicy;
import com.twohands.commerce_service.domain.payment.PaymentWebhookSummary;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaymentSupportReconciliationPolicyTest {

	@Test
	void resolve_returnsNotApplicableForNonPayos() {
		assertEquals(
				"NOT_APPLICABLE",
				PaymentSupportReconciliationPolicy.resolve(PaymentMethod.COD, PaymentStatus.PENDING, List.of())
		);
	}

	@Test
	void resolve_returnsReconciledWhenPaidAndValidWebhookProcessed() {
		List<PaymentWebhookSummary> events = List.of(new PaymentWebhookSummary(
				"PAYOS",
				"PAYMENT_SUCCESS",
				true,
				true,
				Instant.parse("2026-05-20T00:00:00Z")
		));

		assertEquals(
				"RECONCILED",
				PaymentSupportReconciliationPolicy.resolve(PaymentMethod.PAYOS, PaymentStatus.PAID, events)
		);
	}

	@Test
	void resolve_returnsOutstandingWhenPaidWithoutValidWebhook() {
		assertEquals(
				"OUTSTANDING",
				PaymentSupportReconciliationPolicy.resolve(PaymentMethod.PAYOS, PaymentStatus.PAID, List.of())
		);
	}
}
