package com.twohands.admin_service.domain.integration;

import com.twohands.admin_service.domain.support.OrderSupportDetail;

import java.util.UUID;

public interface CommerceOrderSupportGateway {

	boolean isEnabled();

	OrderSupportDetail fetchOrderSupportDetail(UUID orderId, String bearerToken);
}
