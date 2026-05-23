package com.twohands.admin_service.application.support.viewshipmentdetail;

import com.twohands.admin_service.domain.support.ShipmentSupportDetail;

public record ViewShipmentSupportDetailResult(ShipmentSupportDetail detail, boolean contactFieldsMasked) {
}
