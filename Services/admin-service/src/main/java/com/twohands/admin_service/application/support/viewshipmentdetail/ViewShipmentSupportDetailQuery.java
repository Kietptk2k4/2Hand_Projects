package com.twohands.admin_service.application.support.viewshipmentdetail;

import java.util.UUID;

public record ViewShipmentSupportDetailQuery(UUID shipmentId, String bearerToken) {
}
