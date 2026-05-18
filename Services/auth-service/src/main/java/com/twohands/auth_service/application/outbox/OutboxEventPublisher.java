package com.twohands.auth_service.application.outbox;

import com.twohands.auth_service.domain.outbox.OutboxEvent;

public interface OutboxEventPublisher {
    void publish(OutboxEvent event);
}
