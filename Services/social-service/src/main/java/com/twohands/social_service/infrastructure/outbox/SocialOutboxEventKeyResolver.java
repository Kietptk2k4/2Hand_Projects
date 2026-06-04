package com.twohands.social_service.infrastructure.outbox;

import com.twohands.social_service.domain.outbox.OutboxEvent;
import org.springframework.stereotype.Component;

@Component
public class SocialOutboxEventKeyResolver {

    public String resolve(OutboxEvent event) {
        if (event.aggregateId() != null && !event.aggregateId().isBlank()) {
            String normalizedType = event.eventType().trim().toLowerCase().replace('_', '.');
            return "social." + normalizedType + ":" + event.aggregateId();
        }
        return event.id().toString();
    }
}
