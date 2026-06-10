package com.twohands.admin_service.infrastructure.integration;

import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.integration.CommerceOrderSupportGateway;
import com.twohands.admin_service.domain.support.OrderSupportDetail;
import com.twohands.admin_service.domain.support.OrderSupportListEntry;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "admin.integrations.commerce.enabled", havingValue = "false", matchIfMissing = true)
public class DisabledCommerceOrderSupportGateway implements CommerceOrderSupportGateway {

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public PagedResult<OrderSupportListEntry> searchOrders(
			String status,
			String paymentMethod,
			String from,
			String to,
			String sort,
			Integer page,
			Integer size,
			String bearerToken
	) {
		throw new AppException(
				ErrorCode.SERVICE_UNAVAILABLE,
				"Commerce integration is disabled; order support list is unavailable"
		);
	}

	@Override
	public OrderSupportDetail fetchOrderSupportDetail(UUID orderId, String bearerToken) {
		throw new AppException(
				ErrorCode.SERVICE_UNAVAILABLE,
				"Commerce integration is disabled; order support detail is unavailable"
		);
	}
}
