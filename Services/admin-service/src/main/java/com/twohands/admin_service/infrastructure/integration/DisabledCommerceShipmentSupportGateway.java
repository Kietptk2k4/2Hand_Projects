package com.twohands.admin_service.infrastructure.integration;

import com.twohands.admin_service.domain.integration.CommerceShipmentSupportGateway;
import com.twohands.admin_service.domain.support.ShipmentSupportDetail;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "admin.integrations.commerce.enabled", havingValue = "false", matchIfMissing = true)
public class DisabledCommerceShipmentSupportGateway implements CommerceShipmentSupportGateway {

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public ShipmentSupportDetail fetchShipmentSupportDetail(UUID shipmentId, String bearerToken) {
		throw new AppException(
				ErrorCode.SERVICE_UNAVAILABLE,
				"Commerce integration is disabled; shipment support detail is unavailable"
		);
	}
}
