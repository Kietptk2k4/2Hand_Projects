package com.twohands.admin_service.application.support.viewshipmentsupportlist;

import com.twohands.admin_service.domain.support.ShipmentSupportListEntry;

import java.util.List;

public record ViewShipmentSupportListResult(
		int page,
		int size,
		long totalElements,
		int totalPages,
		List<ShipmentSupportListEntry> shipments
) {
}
