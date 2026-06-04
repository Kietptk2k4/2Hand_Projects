package com.twohands.commerce_service.unit.infrastructure.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxStatus;
import com.twohands.commerce_service.infrastructure.outbox.CommerceOutboxMessageBuilder;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CommerceOutboxMessageBuilderTest {

    private final CommerceOutboxMessageBuilder builder = new CommerceOutboxMessageBuilder(new ObjectMapper());

    @Test
    void shouldBuildStandardEnvelopeWithParsedPayload() {
        UUID eventId = UUID.randomUUID();
        UUID aggregateId = UUID.randomUUID();
        Instant occurredAt = Instant.parse("2026-05-20T08:00:00Z");
        OutboxEvent event = new OutboxEvent(
                eventId,
                "COMMERCE_ORDER_CREATED",
                "order:" + aggregateId + ":created",
                aggregateId,
                "commerce",
                "{\"order_id\":\"" + aggregateId + "\",\"final_amount\":100}",
                OutboxStatus.PENDING,
                0,
                occurredAt,
                null,
                null
        );

        Map<String, Object> envelope = builder.buildEnvelope(event);

        assertThat(envelope.get("event_id")).isEqualTo(eventId.toString());
        assertThat(envelope.get("event_type")).isEqualTo("COMMERCE_ORDER_CREATED");
        assertThat(envelope.get("event_key")).isEqualTo("order:" + aggregateId + ":created");
        assertThat(envelope.get("aggregate_id")).isEqualTo(aggregateId.toString());
        assertThat(envelope.get("source")).isEqualTo("commerce");
        assertThat(envelope.get("occurred_at")).isEqualTo(occurredAt.toString());
        assertThat(envelope.get("payload")).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) envelope.get("payload");
        assertThat(payload.get("order_id")).isEqualTo(aggregateId.toString());
        assertThat(payload.get("final_amount")).isEqualTo(100);
    }

    @Test
    void shouldSetRecipientUserIdsFromBuyerIdInPayload() {
        UUID buyerId = UUID.randomUUID();
        UUID aggregateId = UUID.randomUUID();
        OutboxEvent event = new OutboxEvent(
                UUID.randomUUID(),
                "COMMERCE_PAYMENT_PAID",
                "payment:paid",
                aggregateId,
                "commerce",
                "{\"order_id\":\"" + aggregateId + "\",\"buyer_id\":\"" + buyerId + "\"}",
                OutboxStatus.PENDING,
                0,
                Instant.now(),
                null,
                null
        );

        Map<String, Object> envelope = builder.buildEnvelope(event);

        @SuppressWarnings("unchecked")
        java.util.List<String> recipients = (java.util.List<String>) envelope.get("recipient_user_ids");
        assertThat(recipients).containsExactly(buyerId.toString());
    }

    @Test
    void shouldSerializeEnvelopeToJson() {
        UUID eventId = UUID.randomUUID();
        UUID aggregateId = UUID.randomUUID();
        OutboxEvent event = new OutboxEvent(
                eventId,
                "COMMERCE_PAYMENT_PAID",
                "payment:" + aggregateId + ":paid",
                aggregateId,
                "commerce",
                "{\"payment_id\":\"" + aggregateId + "\"}",
                OutboxStatus.PENDING,
                0,
                Instant.now(),
                null,
                null
        );

        String json = builder.buildEnvelopeJson(event);

        assertThat(json).contains("\"event_id\":\"" + eventId + "\"");
        assertThat(json).contains("\"event_type\":\"COMMERCE_PAYMENT_PAID\"");
        assertThat(json).contains("\"payment_id\":\"" + aggregateId + "\"");
    }
}
