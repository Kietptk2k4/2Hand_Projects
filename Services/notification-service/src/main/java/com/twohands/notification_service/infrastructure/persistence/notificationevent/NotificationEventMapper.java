package com.twohands.notification_service.infrastructure.persistence.notificationevent;

import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventPayloadCodec;

final class NotificationEventMapper {

    private NotificationEventMapper() {
    }

    static NotificationEvent toDomain(NotificationEventEntity entity) {
        return new NotificationEvent(
                entity.getId(),
                entity.getSourceEventId(),
                entity.getEventKey(),
                entity.getEventType(),
                entity.getSourceService(),
                entity.getAggregateType(),
                entity.getAggregateId(),
                entity.getActorId(),
                entity.getRecipientUserId(),
                NotificationEventPayloadCodec.decode(entity.getPayload()),
                entity.getStatus(),
                entity.getRetryCount(),
                entity.getMaxRetryCount(),
                entity.getLastError(),
                entity.getLockedAt(),
                entity.getLockedBy(),
                entity.getCreatedAt(),
                entity.getProcessedAt()
        );
    }

    static NotificationEventEntity toEntity(NotificationEvent event) {
        NotificationEventEntity entity = new NotificationEventEntity();
        entity.setId(event.id());
        entity.setSourceEventId(event.sourceEventId());
        entity.setEventKey(event.eventKey());
        entity.setEventType(event.eventType());
        entity.setSourceService(event.sourceService());
        entity.setAggregateType(event.aggregateType());
        entity.setAggregateId(event.aggregateId());
        entity.setActorId(event.actorId());
        entity.setRecipientUserId(event.recipientUserId());
        entity.setPayload(event.payload());
        entity.setStatus(event.status());
        entity.setRetryCount(event.retryCount());
        entity.setMaxRetryCount(event.maxRetryCount());
        entity.setLastError(event.lastError());
        entity.setLockedAt(event.lockedAt());
        entity.setLockedBy(event.lockedBy());
        entity.setCreatedAt(event.createdAt());
        entity.setProcessedAt(event.processedAt());
        return entity;
    }
}
