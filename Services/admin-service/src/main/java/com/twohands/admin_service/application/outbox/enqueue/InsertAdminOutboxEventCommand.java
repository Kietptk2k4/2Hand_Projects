package com.twohands.admin_service.application.outbox.enqueue;

import java.util.UUID;

public record InsertAdminOutboxEventCommand(
		String eventType,
		UUID aggregateId,
		String payloadJson
) {
}
