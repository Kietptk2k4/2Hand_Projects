package com.twohands.commerce_service.domain.payment;

import java.util.Optional;

public interface PaymentWebhookLogRepository {

    record WebhookLogRecord(
            boolean signatureValid,
            boolean alreadyProcessed
    ) {
    }

    WebhookLogRecord recordPayosWebhook(
            String eventType,
            String payosOrderCode,
            String payloadJson,
            boolean signatureValid
    );

    Optional<WebhookLogRecord> findByPayosEvent(String eventType, String payosOrderCode);

    void markProcessed(String eventType, String payosOrderCode);
}
