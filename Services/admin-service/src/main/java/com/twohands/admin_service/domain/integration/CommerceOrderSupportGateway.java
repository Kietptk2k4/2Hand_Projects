package com.twohands.admin_service.domain.integration;

import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.support.OrderSupportDetail;
import com.twohands.admin_service.domain.support.OrderSupportListEntry;

import java.util.UUID;

public interface CommerceOrderSupportGateway {

	boolean isEnabled();

	PagedResult<OrderSupportListEntry> searchOrders(
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
	);

	OrderSupportDetail fetchOrderSupportDetail(UUID orderId, String bearerToken);
}
