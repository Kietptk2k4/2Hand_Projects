package com.twohands.admin_service.domain.support;

import java.util.UUID;

public record ShipmentSupportOrderItem(
		UUID orderItemId,
		String productNameSnapshot,
		int quantity,
		String status
) {
}
