package com.twohands.admin_service.application.support.viewordersfor;

public record ViewOrdersForSupportQuery(
		String status,
		String paymentMethod,
		String paymentStatus,
		String q,
		String from,
		String to,
		String sort,
		Integer page,
		Integer size,
		String bearerToken
) {
}
