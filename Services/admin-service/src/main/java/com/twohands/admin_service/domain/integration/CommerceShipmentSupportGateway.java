package com.twohands.admin_service.domain.integration;

import com.twohands.admin_service.domain.support.ShipmentSupportDetail;

import java.util.UUID;

public interface CommerceShipmentSupportGateway {

	boolean isEnabled();

	ShipmentSupportDetail fetchShipmentSupportDetail(UUID shipmentId, String bearerToken);
}
