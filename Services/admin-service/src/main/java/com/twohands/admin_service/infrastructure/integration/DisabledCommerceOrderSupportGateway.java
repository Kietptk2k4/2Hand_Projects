package com.twohands.admin_service.infrastructure.integration;

import com.twohands.admin_service.domain.integration.CommerceOrderSupportGateway;
import com.twohands.admin_service.domain.support.OrderSupportDetail;
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
	public OrderSupportDetail fetchOrderSupportDetail(UUID orderId, String bearerToken) {
		throw new AppException(
				ErrorCode.SERVICE_UNAVAILABLE,
				"Commerce integration is disabled; order support detail is unavailable"
		);
	}
}
