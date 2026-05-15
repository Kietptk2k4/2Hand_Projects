package com.twohands.auth_service.domain.outbox;

public interface OutboxEventRepository {
    OutboxEvent save(OutboxEvent event);
}
