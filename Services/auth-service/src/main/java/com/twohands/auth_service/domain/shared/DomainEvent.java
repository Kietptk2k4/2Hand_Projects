package com.twohands.auth_service.domain.shared;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredAt();
}
