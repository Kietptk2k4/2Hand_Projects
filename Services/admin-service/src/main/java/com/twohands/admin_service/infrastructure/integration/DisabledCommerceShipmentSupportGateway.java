package com.twohands.admin_service.infrastructure.integration;

import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.integration.CommerceShipmentSupportGateway;
import com.twohands.admin_service.domain.support.AdminOverrideShipmentStatusResult;
import com.twohands.admin_service.domain.support.ShipmentSupportDetail;
import com.twohands.admin_service.domain.support.ShipmentSupportListEntry;
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
	public PagedResult<ShipmentSupportListEntry> listShipmentSupport(
			String status,
			String carrier,
			String sort,
			Integer page,
			Integer size,
			String bearerToken
	) {
		throw new AppException(
				ErrorCode.SERVICE_UNAVAILABLE,
				"Commerce integration is disabled; shipment support list is unavailable"
		);
	}

	@Override
	public ShipmentSupportDetail fetchShipmentSupportDetail(UUID shipmentId, String bearerToken) {
		throw new AppException(
				ErrorCode.SERVICE_UNAVAILABLE,
				"Commerce integration is disabled; shipment support detail is unavailable"
		);
	}

	@Override
	public AdminOverrideShipmentStatusResult overrideShipmentStatus(
			UUID shipmentId,
			String status,
			String reason,
			boolean force,
			String bearerToken
	) {
		throw new AppException(
				ErrorCode.SERVICE_UNAVAILABLE,
				"Commerce integration is disabled; shipment status override is unavailable"
		);
	}
}
