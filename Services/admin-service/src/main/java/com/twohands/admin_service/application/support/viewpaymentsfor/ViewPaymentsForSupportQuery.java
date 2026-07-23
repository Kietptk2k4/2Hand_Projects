package com.twohands.admin_service.application.support.viewpaymentsfor;

public record ViewPaymentsForSupportQuery(
		String status,
		String paymentMethod,
		String orderId,
		String q,
		String reconciliationStatus,
		String from,
		String to,
		Integer page,
		Integer size,
		String bearerToken
) {
}
