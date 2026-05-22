package com.twohands.commerce_service.delivery.http.payment;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PayosWebhookResponse(
        @JsonProperty("event_type") String eventType,
        @JsonProperty("payos_order_code") String payosOrderCode,
        @JsonProperty("signature_valid") boolean signatureValid,
        boolean processed,
        @JsonProperty("terminal_status") String terminalStatus,
        @JsonProperty("failure_outcome") String failureOutcome,
        @JsonProperty("success_webhook") boolean successWebhook
) {
}
