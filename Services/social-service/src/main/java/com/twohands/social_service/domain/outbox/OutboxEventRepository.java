package com.twohands.social_service.domain.outbox;

public interface OutboxEventRepository {
    OutboxEvent save(OutboxEvent event);
}
