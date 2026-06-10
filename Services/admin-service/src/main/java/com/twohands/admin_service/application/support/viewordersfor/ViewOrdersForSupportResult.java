package com.twohands.admin_service.application.support.viewordersfor;

import com.twohands.admin_service.domain.support.OrderSupportListEntry;

import java.util.List;

public record ViewOrdersForSupportResult(
		int page,
		int size,
		long totalElements,
		int totalPages,
		List<OrderSupportListEntry> orders
) {
}
