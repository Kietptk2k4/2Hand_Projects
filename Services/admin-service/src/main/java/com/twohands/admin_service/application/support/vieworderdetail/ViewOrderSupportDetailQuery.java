package com.twohands.admin_service.application.support.vieworderdetail;

import java.util.UUID;

public record ViewOrderSupportDetailQuery(UUID orderId, String bearerToken) {
}
