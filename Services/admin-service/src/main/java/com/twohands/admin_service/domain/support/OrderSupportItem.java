package com.twohands.admin_service.domain.support;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderSupportItem(
		UUID orderItemId,
		UUID productId,
		UUID sellerId,
		UUID shipmentId,
		int quantity,
		String status,
		BigDecimal unitPriceSnapshot,
		BigDecimal finalPrice,
		String skuSnapshot,
		String productNameSnapshot,
		String imageSnapshot,
		String attributesSnapshot,
		String shopNameSnapshot,
		BigDecimal shippingFeeAllocated,
		Instant completedAt
) {
}
