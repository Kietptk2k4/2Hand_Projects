package com.twohands.notification_service.infrastructure.messaging;

import com.twohands.notification_service.application.ingest.IngestNotificationEventResult;
import com.twohands.notification_service.application.ingest.NotificationEventIngestCommand;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventRepository;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class DbNotificationEventConsumer implements NotificationEventConsumer {

    private final NotificationEventRepository notificationEventRepository;

    public DbNotificationEventConsumer(NotificationEventRepository notificationEventRepository) {
        this.notificationEventRepository = notificationEventRepository;
    }

    @Override
    @Transactional
    public IngestNotificationEventResult consume(NotificationEventIngestCommand command) {
        Optional<NotificationEvent> existing = findExisting(command);
        if (existing.isPresent()) {
            return new IngestNotificationEventResult(existing.get().id(), true);
        }

        NotificationEvent event = new NotificationEvent(
                UUID.randomUUID(),
                command.sourceEventId(),
                command.eventKey(),
                command.eventType(),
                command.sourceService(),
                command.aggregateType(),
                command.aggregateId(),
                command.actorId(),
                command.recipientUserId(),
                normalizePayload(command.payload()),
                NotificationEventStatus.PENDING,
                0,
                5,
                null,
                null,
                null,
                Instant.now(),
                null
        );

        NotificationEvent saved = notificationEventRepository.save(event);
        return new IngestNotificationEventResult(saved.id(), false);
    }

    private Optional<NotificationEvent> findExisting(NotificationEventIngestCommand command) {
        if (command.sourceEventId() != null) {
            Optional<NotificationEvent> bySourceEventId = notificationEventRepository
                    .findBySourceServiceAndSourceEventId(command.sourceService(), command.sourceEventId());
            if (bySourceEventId.isPresent()) {
                return bySourceEventId;
            }
        }
        if (command.eventKey() != null && !command.eventKey().isBlank()) {
            return notificationEventRepository.findBySourceServiceAndEventKey(
                    command.sourceService(),
                    command.eventKey()
            );
        }
        return Optional.empty();
    }

    private String normalizePayload(String payload) {
        if (payload == null || payload.isBlank()) {
            return "{}";
        }
        return payload;
    }
}
