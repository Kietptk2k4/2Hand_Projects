package com.twohands.notification_service.application.consume;

import com.twohands.notification_service.application.ingest.IngestNotificationEventResult;
import com.twohands.notification_service.application.ingest.IngestNotificationEventUseCase;
import com.twohands.notification_service.application.ingest.NotificationEventIngestCommand;
import com.twohands.notification_service.exception.AppException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsumeDomainEventUseCase {

    private final DomainEventMessageParser messageParser;
    private final IngestNotificationEventUseCase ingestNotificationEventUseCase;

    public ConsumeDomainEventUseCase(
            DomainEventMessageParser messageParser,
            IngestNotificationEventUseCase ingestNotificationEventUseCase
    ) {
        this.messageParser = messageParser;
        this.ingestNotificationEventUseCase = ingestNotificationEventUseCase;
    }

    @Transactional
    public ConsumeDomainEventResult execute(String rawMessage, String topic) {
        ConsumeDomainEventCommand command = messageParser.parse(rawMessage, topic);
        try {
            IngestNotificationEventResult result = ingestNotificationEventUseCase.execute(toIngestCommand(command));
            return new ConsumeDomainEventResult(result.notificationEventId(), result.duplicate());
        } catch (AppException ex) {
            throw new InvalidDomainEventException(ex.getMessage(), ex);
        }
    }

    private NotificationEventIngestCommand toIngestCommand(ConsumeDomainEventCommand command) {
        return new NotificationEventIngestCommand(
                command.eventId(),
                command.eventKey(),
                command.eventType(),
                command.sourceService(),
                command.aggregateType(),
                command.aggregateId(),
                command.actorId(),
                command.recipientUserId(),
                command.payloadJson()
        );
    }
}
