package com.twohands.admin_service.application.support.viewshipmentsupportlist;

public record ViewShipmentSupportListQuery(
		String status,
		String carrier,
		String sort,
		Integer page,
		Integer size,
		String bearerToken
) {
}
