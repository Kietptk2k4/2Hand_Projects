package com.twohands.notification_service.unit.application.ingest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.idempotency.EnsureNotificationEventIdempotencyUseCase;
import com.twohands.notification_service.application.email.AccountEnforcementEmailPayloadNormalizer;
import com.twohands.notification_service.application.email.AuthSecurityEmailNotificationPayloadNormalizer;
import com.twohands.notification_service.application.email.CommerceOrderNotificationPayloadNormalizer;
import com.twohands.notification_service.application.ingest.JacksonNotificationEventPayloadSanitizer;
import com.twohands.notification_service.application.ingest.NotificationEventIngestCommand;
import com.twohands.notification_service.application.ingest.StoreNotificationEventUseCase;
import com.twohands.notification_service.config.NotificationEmailProperties;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventPayloadSanitizer;
import com.twohands.notification_service.domain.notificationevent.NotificationEventRepository;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreNotificationEventUseCaseTest {

    @Mock
    private NotificationEventRepository notificationEventRepository;

  private NotificationEventPayloadSanitizer payloadSanitizer;

    private StoreNotificationEventUseCase useCase;

    @BeforeEach
    void setUp() {
        payloadSanitizer = new JacksonNotificationEventPayloadSanitizer(new ObjectMapper());
        EnsureNotificationEventIdempotencyUseCase ensureIdempotency =
                new EnsureNotificationEventIdempotencyUseCase(notificationEventRepository);
        NotificationEmailProperties emailProperties = new NotificationEmailProperties();
        emailProperties.setVerificationLinkBaseUrl("https://2hands.vn/verify-email");
        emailProperties.setPasswordResetLinkBaseUrl("https://2hands.vn/reset-password");
        AuthSecurityEmailNotificationPayloadNormalizer authSecurityEmailNormalizer =
                new AuthSecurityEmailNotificationPayloadNormalizer(new ObjectMapper(), emailProperties);
        AccountEnforcementEmailPayloadNormalizer accountEnforcementNormalizer =
                new AccountEnforcementEmailPayloadNormalizer(new ObjectMapper());
        CommerceOrderNotificationPayloadNormalizer commerceOrderNormalizer =
                new CommerceOrderNotificationPayloadNormalizer(new ObjectMapper());
        useCase = new StoreNotificationEventUseCase(
                notificationEventRepository,
                authSecurityEmailNormalizer,
                accountEnforcementNormalizer,
                commerceOrderNormalizer,
                payloadSanitizer,
                ensureIdempotency
        );
    }

    @Test
    void execute_storesPendingEventWithSanitizedPayload() {
        UUID sourceEventId = UUID.randomUUID();
        UUID savedId = UUID.randomUUID();
        NotificationEventIngestCommand command = command(
                sourceEventId,
                null,
                "{\"actorName\":\"Alice\",\"password\":\"secret\"}"
        );

        when(notificationEventRepository.findBySourceServiceAndSourceEventId(NotificationSourceService.SOCIAL, sourceEventId))
                .thenReturn(Optional.empty());
        when(notificationEventRepository.save(any(NotificationEvent.class))).thenAnswer(invocation -> {
            NotificationEvent event = invocation.getArgument(0);
            return new NotificationEvent(
                    savedId,
                    event.sourceEventId(),
                    event.eventKey(),
                    event.eventType(),
                    event.sourceService(),
                    event.aggregateType(),
                    event.aggregateId(),
                    event.actorId(),
                    event.recipientUserId(),
                    event.payload(),
                    event.status(),
                    event.retryCount(),
                    event.maxRetryCount(),
                    event.lastError(),
                    event.lockedAt(),
                    event.lockedBy(),
                    event.createdAt(),
                    event.processedAt()
            );
        });

        var result = useCase.execute(command);

        assertFalse(result.duplicate());
        assertEquals(savedId, result.notificationEventId());

        ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(notificationEventRepository).save(captor.capture());
        NotificationEvent saved = captor.getValue();
        assertEquals(NotificationEventStatus.PENDING, saved.status());
        assertEquals(0, saved.retryCount());
        assertTrue(saved.payload().contains("***REDACTED***"));
        assertFalse(saved.payload().contains("secret"));
    }

    @Test
    void execute_returnsDuplicateWhenSourceEventIdExists() {
        UUID sourceEventId = UUID.randomUUID();
        UUID existingId = UUID.randomUUID();
        NotificationEventIngestCommand command = command(sourceEventId, null, "{}");
        NotificationEvent existing = existingEvent(existingId, sourceEventId, null);

        when(notificationEventRepository.findBySourceServiceAndSourceEventId(NotificationSourceService.SOCIAL, sourceEventId))
                .thenReturn(Optional.of(existing));

        var result = useCase.execute(command);

        assertTrue(result.duplicate());
        assertEquals(existingId, result.notificationEventId());
        verify(notificationEventRepository, never()).save(any());
    }

    @Test
    void execute_returnsDuplicateWhenEventKeyExists() {
        String eventKey = "social.post.post-id.liked";
        UUID existingId = UUID.randomUUID();
        NotificationEventIngestCommand command = new NotificationEventIngestCommand(
                null,
                eventKey,
                "POST_LIKED",
                NotificationSourceService.SOCIAL,
                "POST",
                "post-id",
                null,
                null,
                "{}"
        );
        NotificationEvent existing = existingEvent(existingId, null, eventKey);

        when(notificationEventRepository.findBySourceServiceAndEventKey(NotificationSourceService.SOCIAL, eventKey))
                .thenReturn(Optional.of(existing));

        var result = useCase.execute(command);

        assertTrue(result.duplicate());
        assertEquals(existingId, result.notificationEventId());
        verify(notificationEventRepository, never()).save(any());
    }

    @Test
    void execute_treatsUniqueConflictAsDuplicate() {
        UUID sourceEventId = UUID.randomUUID();
        UUID existingId = UUID.randomUUID();
        NotificationEventIngestCommand command = command(sourceEventId, null, "{}");

        when(notificationEventRepository.findBySourceServiceAndSourceEventId(NotificationSourceService.SOCIAL, sourceEventId))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existingEvent(existingId, sourceEventId, null)));
        when(notificationEventRepository.save(any(NotificationEvent.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        var result = useCase.execute(command);

        assertTrue(result.duplicate());
        assertEquals(existingId, result.notificationEventId());
    }

    @Test
    void execute_rejectsMissingIdempotencyKey() {
        NotificationEventIngestCommand command = new NotificationEventIngestCommand(
                null,
                " ",
                "POST_LIKED",
                NotificationSourceService.SOCIAL,
                null,
                null,
                null,
                null,
                "{}"
        );

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(command));

        assertEquals(ErrorCode.MISSING_IDEMPOTENCY_KEY, ex.getErrorCode());
        verify(notificationEventRepository, never()).save(any());
    }

    @Test
    void execute_rejectsInvalidEventType() {
        NotificationEventIngestCommand command = new NotificationEventIngestCommand(
                UUID.randomUUID(),
                null,
                "post_liked",
                NotificationSourceService.SOCIAL,
                null,
                null,
                null,
                null,
                "{}"
        );

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(command));

        assertEquals(ErrorCode.VALIDATION_ERROR, ex.getErrorCode());
        verify(notificationEventRepository, never()).save(any());
    }

    @Test
    void execute_normalizesEmailVerificationPayloadBeforeSanitizing() {
        UUID sourceEventId = UUID.randomUUID();
        UUID savedId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        NotificationEventIngestCommand command = new NotificationEventIngestCommand(
                sourceEventId,
                null,
                "EMAIL_VERIFICATION_REQUESTED",
                NotificationSourceService.AUTH,
                null,
                null,
                null,
                userId,
                """
                        {
                          "user_id":"%s",
                          "email":"user@example.com",
                          "verification_token":"secret-token-value"
                        }
                        """.formatted(userId)
        );

        when(notificationEventRepository.findBySourceServiceAndSourceEventId(NotificationSourceService.AUTH, sourceEventId))
                .thenReturn(Optional.empty());
        when(notificationEventRepository.save(any(NotificationEvent.class))).thenAnswer(invocation -> {
            NotificationEvent event = invocation.getArgument(0);
            return new NotificationEvent(
                    savedId,
                    event.sourceEventId(),
                    event.eventKey(),
                    event.eventType(),
                    event.sourceService(),
                    event.aggregateType(),
                    event.aggregateId(),
                    event.actorId(),
                    event.recipientUserId(),
                    event.payload(),
                    event.status(),
                    event.retryCount(),
                    event.maxRetryCount(),
                    event.lastError(),
                    event.lockedAt(),
                    event.lockedBy(),
                    event.createdAt(),
                    event.processedAt()
            );
        });

        useCase.execute(command);

        ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(notificationEventRepository).save(captor.capture());
        String storedPayload = captor.getValue().payload();
        assertTrue(storedPayload.contains("recipient_email"));
        assertTrue(storedPayload.contains("verification_link"));
        assertFalse(storedPayload.contains("\"verification_token\""));
    }

    @Test
    void execute_normalizesPasswordResetPayloadBeforeSanitizing() {
        UUID sourceEventId = UUID.randomUUID();
        UUID savedId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        NotificationEventIngestCommand command = new NotificationEventIngestCommand(
                sourceEventId,
                null,
                "PASSWORD_RESET_REQUESTED",
                NotificationSourceService.AUTH,
                null,
                null,
                null,
                userId,
                """
                        {
                          "user_id":"%s",
                          "email":"user@example.com",
                          "verification_token":"secret-reset-token"
                        }
                        """.formatted(userId)
        );

        when(notificationEventRepository.findBySourceServiceAndSourceEventId(NotificationSourceService.AUTH, sourceEventId))
                .thenReturn(Optional.empty());
        when(notificationEventRepository.save(any(NotificationEvent.class))).thenAnswer(invocation -> {
            NotificationEvent event = invocation.getArgument(0);
            return new NotificationEvent(
                    savedId,
                    event.sourceEventId(),
                    event.eventKey(),
                    event.eventType(),
                    event.sourceService(),
                    event.aggregateType(),
                    event.aggregateId(),
                    event.actorId(),
                    event.recipientUserId(),
                    event.payload(),
                    event.status(),
                    event.retryCount(),
                    event.maxRetryCount(),
                    event.lastError(),
                    event.lockedAt(),
                    event.lockedBy(),
                    event.createdAt(),
                    event.processedAt()
            );
        });

        useCase.execute(command);

        ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(notificationEventRepository).save(captor.capture());
        String storedPayload = captor.getValue().payload();
        assertTrue(storedPayload.contains("recipient_email"));
        assertTrue(storedPayload.contains("reset_link"));
        assertFalse(storedPayload.contains("\"verification_token\""));
    }

    @Test
    void execute_rejectsInvalidPayloadJson() {
        NotificationEventIngestCommand command = command(UUID.randomUUID(), null, "{invalid");

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(command));

        assertEquals(ErrorCode.INVALID_EVENT_PAYLOAD, ex.getErrorCode());
        verify(notificationEventRepository, never()).save(any());
    }

    private NotificationEventIngestCommand command(UUID sourceEventId, String eventKey, String payload) {
        return new NotificationEventIngestCommand(
                sourceEventId,
                eventKey,
                "POST_LIKED",
                NotificationSourceService.SOCIAL,
                "POST",
                "post-id",
                UUID.randomUUID(),
                UUID.randomUUID(),
                payload
        );
    }

    private NotificationEvent existingEvent(UUID id, UUID sourceEventId, String eventKey) {
        return new NotificationEvent(
                id,
                sourceEventId,
                eventKey,
                "POST_LIKED",
                NotificationSourceService.SOCIAL,
                "POST",
                "post-id",
                null,
                null,
                "{}",
                NotificationEventStatus.PENDING,
                0,
                5,
                null,
                null,
                null,
                Instant.now(),
                null
        );
    }
}
