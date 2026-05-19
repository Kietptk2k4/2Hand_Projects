package com.twohands.social_service.application.outbox;

import com.twohands.social_service.domain.outbox.OutboxEvent;

public interface OutboxEventPublisher {
    void publish(OutboxEvent event);
}
