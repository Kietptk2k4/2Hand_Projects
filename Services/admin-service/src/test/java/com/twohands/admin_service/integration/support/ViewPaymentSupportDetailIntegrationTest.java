package com.twohands.admin_service.integration.support;

import com.twohands.admin_service.application.support.viewpaymentdetail.ViewPaymentSupportDetailQuery;
import com.twohands.admin_service.application.support.viewpaymentdetail.ViewPaymentSupportDetailUseCase;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.integration.CommercePaymentSupportGateway;
import com.twohands.admin_service.domain.support.PaymentSupportDetail;
import com.twohands.admin_service.domain.support.PaymentSupportWebhookEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ViewPaymentSupportDetailIntegrationTest {

	@Autowired
	private ViewPaymentSupportDetailUseCase viewPaymentSupportDetailUseCase;

	@MockBean
	private AdminAuthorizationService adminAuthorizationService;

	@MockBean
	private CommercePaymentSupportGateway commercePaymentSupportGateway;

	@Test
	void execute_returnsPaymentSupportDetailFromCommerceGateway() {
		UUID paymentId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(commercePaymentSupportGateway.isEnabled()).thenReturn(true);
		when(commercePaymentSupportGateway.fetchPaymentSupportDetail(paymentId, "bearer"))
				.thenReturn(buildDetail(paymentId));

		var result = viewPaymentSupportDetailUseCase.execute(new ViewPaymentSupportDetailQuery(paymentId, "bearer"));

		assertEquals(paymentId, result.detail().paymentId());
		assertEquals("OUTSTANDING", result.detail().reconciliationStatus());
		assertFalse(result.detail().checkoutUrlAvailable());
		assertEquals(1, result.detail().webhookEvents().size());
	}

	private PaymentSupportDetail buildDetail(UUID paymentId) {
		return new PaymentSupportDetail(
				paymentId,
				UUID.randomUUID(),
				UUID.randomUUID(),
				"PAYOS",
				BigDecimal.valueOf(99000),
				"VND",
				"PAID",
				Instant.parse("2026-05-20T09:00:00Z"),
				null,
				Instant.parse("2026-05-19T09:00:00Z"),
				Instant.parse("2026-05-20T09:00:00Z"),
				"PAYOS-777",
				"TX-1",
				false,
				null,
				"PROCESSING",
				"PAID",
				"OUTSTANDING",
				List.of(),
				List.of(new PaymentSupportWebhookEvent(
						"PAYOS",
						"PAYMENT_FAILED",
						false,
						false,
						Instant.parse("2026-05-20T08:30:00Z")
				))
		);
	}
}
