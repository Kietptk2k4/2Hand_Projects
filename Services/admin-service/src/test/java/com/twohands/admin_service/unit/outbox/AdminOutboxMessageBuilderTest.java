package com.twohands.admin_service.unit.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.admin_service.domain.outbox.OutboxEvent;
import com.twohands.admin_service.domain.outbox.OutboxStatus;
import com.twohands.admin_service.infrastructure.outbox.AdminOutboxEventKeyResolver;
import com.twohands.admin_service.infrastructure.outbox.AdminOutboxMessageBuilder;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AdminOutboxMessageBuilderTest {

	private final AdminOutboxMessageBuilder builder = new AdminOutboxMessageBuilder(
			new ObjectMapper(),
			new AdminOutboxEventKeyResolver()
	);

	@Test
	void buildEnvelope_includesEventIdForIdempotentConsumers() {
		UUID eventId = UUID.randomUUID();
		UUID aggregateId = UUID.randomUUID();
		OutboxEvent event = new OutboxEvent(
				eventId,
				"USER_SUSPENDED",
				aggregateId,
				"{\"user_id\":\"" + aggregateId + "\"}",
				OutboxStatus.PENDING,
				0,
				Instant.parse("2026-05-19T10:00:00Z"),
				null,
				null
		);

		Map<String, Object> envelope = builder.buildEnvelope(event);

		assertEquals(eventId.toString(), envelope.get("event_id"));
		assertEquals("USER_SUSPENDED", envelope.get("event_type"));
		assertEquals("admin", envelope.get("source"));
		assertNotNull(envelope.get("event_key"));
		assertNotNull(envelope.get("payload"));
	}

	@Test
	void buildEnvelope_setsRecipientUserIdsFromSellerUserId() {
		UUID sellerUserId = UUID.randomUUID();
		UUID aggregateId = UUID.randomUUID();
		OutboxEvent event = new OutboxEvent(
				UUID.randomUUID(),
				"PRODUCT_REMOVED",
				aggregateId,
				"{\"product_id\":\"" + aggregateId + "\",\"seller_user_id\":\"" + sellerUserId + "\"}",
				OutboxStatus.PENDING,
				0,
				Instant.parse("2026-05-19T10:00:00Z"),
				null,
				null
		);

		@SuppressWarnings("unchecked")
		java.util.List<String> recipients = (java.util.List<String>) builder.buildEnvelope(event).get("recipient_user_ids");

		assertEquals(sellerUserId.toString(), recipients.get(0));
	}

	@Test
	void buildEnvelope_setsRecipientUserIdsFromUserIdForEnforcement() {
		UUID userId = UUID.randomUUID();
		OutboxEvent event = new OutboxEvent(
				UUID.randomUUID(),
				"USER_SUSPENDED",
				userId,
				"{\"user_id\":\"" + userId + "\",\"enforcement_id\":\"" + UUID.randomUUID() + "\"}",
				OutboxStatus.PENDING,
				0,
				Instant.parse("2026-05-19T10:00:00Z"),
				null,
				null
		);

		@SuppressWarnings("unchecked")
		java.util.List<String> recipients = (java.util.List<String>) builder.buildEnvelope(event).get("recipient_user_ids");

		assertEquals(userId.toString(), recipients.get(0));
	}
}
