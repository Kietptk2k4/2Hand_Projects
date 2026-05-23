package com.twohands.notification_service.unit.application.consume;

import com.twohands.notification_service.application.consume.ConsumeDomainEventCommand;
import com.twohands.notification_service.application.consume.ConsumeDomainEventUseCase;
import com.twohands.notification_service.application.consume.DomainEventMessageParser;
import com.twohands.notification_service.application.consume.InvalidDomainEventException;
import com.twohands.notification_service.application.ingest.IngestNotificationEventResult;
import com.twohands.notification_service.application.ingest.IngestNotificationEventUseCase;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsumeDomainEventUseCaseTest {

    @Mock
    private DomainEventMessageParser messageParser;

    @Mock
    private IngestNotificationEventUseCase ingestNotificationEventUseCase;

    @InjectMocks
    private ConsumeDomainEventUseCase useCase;

    @Test
    void execute_routesParsedEventToIngest() {
        UUID eventId = UUID.randomUUID();
        UUID notificationEventId = UUID.randomUUID();
        ConsumeDomainEventCommand command = new ConsumeDomainEventCommand(
                eventId,
                "POST_LIKED",
                NotificationSourceService.SOCIAL,
                "social.post.post-id.liked",
                "POST",
                "post-id",
                UUID.randomUUID(),
                UUID.randomUUID(),
                "{\"post_id\":\"post-id\"}",
                null
        );

        when(messageParser.parse("{}", "social.post.liked")).thenReturn(command);
        when(ingestNotificationEventUseCase.execute(any())).thenReturn(
                new IngestNotificationEventResult(notificationEventId, false)
        );

        var result = useCase.execute("{}", "social.post.liked");

        assertEquals(notificationEventId, result.notificationEventId());
        assertFalse(result.duplicate());
        verify(ingestNotificationEventUseCase).execute(any());
    }

    @Test
    void execute_wrapsIngestValidationFailureAsInvalidDomainEvent() {
        when(messageParser.parse("{}", "social.post.liked")).thenReturn(sampleCommand());
        when(ingestNotificationEventUseCase.execute(any()))
                .thenThrow(new AppException(ErrorCode.MISSING_IDEMPOTENCY_KEY, "missing key"));

        assertThrows(InvalidDomainEventException.class, () -> useCase.execute("{}", "social.post.liked"));
    }

    private ConsumeDomainEventCommand sampleCommand() {
        return new ConsumeDomainEventCommand(
                UUID.randomUUID(),
                "POST_LIKED",
                NotificationSourceService.SOCIAL,
                null,
                null,
                null,
                null,
                null,
                "{}",
                null
        );
    }
}
