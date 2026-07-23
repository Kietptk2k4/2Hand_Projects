package com.twohands.admin_service.application.support.viewshipmentsupportlist;

public record ViewShipmentSupportListQuery(
		String status,
		String carrier,
		String sort,
		String q,
		String orderId,
		String from,
		String to,
		Integer page,
		Integer size,
		String bearerToken
) {
}
