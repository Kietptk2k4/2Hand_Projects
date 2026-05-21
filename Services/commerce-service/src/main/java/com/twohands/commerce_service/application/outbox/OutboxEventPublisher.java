package com.twohands.commerce_service.application.outbox;

import com.twohands.commerce_service.domain.outbox.OutboxEvent;

public interface OutboxEventPublisher {
    void publish(OutboxEvent event);
}
