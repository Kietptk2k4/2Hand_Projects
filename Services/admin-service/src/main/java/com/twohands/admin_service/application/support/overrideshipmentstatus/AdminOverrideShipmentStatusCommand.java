package com.twohands.admin_service.application.support.overrideshipmentstatus;

import java.util.UUID;

public record AdminOverrideShipmentStatusCommand(
		UUID shipmentId,
		String status,
		String reason,
		boolean force,
		String bearerToken
) {
}
