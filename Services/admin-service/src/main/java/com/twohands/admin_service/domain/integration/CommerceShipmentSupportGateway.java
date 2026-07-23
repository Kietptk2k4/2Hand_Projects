package com.twohands.admin_service.domain.integration;

import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.support.AdminOverrideShipmentStatusResult;
import com.twohands.admin_service.domain.support.ShipmentSupportDetail;
import com.twohands.admin_service.domain.support.ShipmentSupportListEntry;

import java.util.UUID;

public interface CommerceShipmentSupportGateway {

	boolean isEnabled();

	ShipmentSupportDetail fetchShipmentSupportDetail(UUID shipmentId, String bearerToken);

	PagedResult<ShipmentSupportListEntry> listShipmentSupport(
			String status,
			String carrier,
			String sort,
			String q,
			String orderId,
			String from,
			String to,
			Integer page,
			Integer size,
			String bearerToken
	);

	AdminOverrideShipmentStatusResult overrideShipmentStatus(
			UUID shipmentId,
			String status,
			String reason,
			boolean force,
			String bearerToken
	);
}
