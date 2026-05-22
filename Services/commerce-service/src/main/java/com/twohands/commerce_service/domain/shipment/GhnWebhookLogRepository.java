package com.twohands.commerce_service.domain.shipment;

import java.util.UUID;

public interface GhnWebhookLogRepository {

    UUID insertLog(String ghnOrderCode, String rawStatus, String payloadJson);

    void markProcessed(UUID logId);
}
