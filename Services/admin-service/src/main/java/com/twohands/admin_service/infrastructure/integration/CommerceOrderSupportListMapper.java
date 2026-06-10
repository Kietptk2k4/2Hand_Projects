package com.twohands.admin_service.infrastructure.integration;

import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.support.OrderSupportListEntry;

import java.util.List;

final class CommerceOrderSupportListMapper {

	private CommerceOrderSupportListMapper() {
	}

	static PagedResult<OrderSupportListEntry> toDomain(CommerceOrdersSupportPayload payload) {
		if (payload == null) {
			return new PagedResult<>(List.of(), 1, 20, 0L, 0);
		}
		List<OrderSupportListEntry> items = payload.orders() == null
				? List.of()
				: payload.orders().stream().map(CommerceOrderSupportListMapper::toEntry).toList();
		return new PagedResult<>(
				items,
				payload.page(),
				payload.size(),
				payload.totalElements(),
				payload.totalPages()
		);
	}

	private static OrderSupportListEntry toEntry(CommerceOrdersSupportPayload.OrderListPayload order) {
		return new OrderSupportListEntry(
				order.orderId(),
				order.buyerId(),
				order.orderStatus(),
				order.paymentStatus(),
				order.paymentMethod(),
				order.finalAmount(),
				order.createdAt(),
				order.updatedAt()
		);
	}
}
