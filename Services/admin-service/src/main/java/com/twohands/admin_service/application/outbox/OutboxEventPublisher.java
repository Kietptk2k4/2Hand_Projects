package com.twohands.admin_service.application.outbox;

import com.twohands.admin_service.domain.outbox.OutboxEvent;

public interface OutboxEventPublisher {
	void publish(OutboxEvent event);
}
