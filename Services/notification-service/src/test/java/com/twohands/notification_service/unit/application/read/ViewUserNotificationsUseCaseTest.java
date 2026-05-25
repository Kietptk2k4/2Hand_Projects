package com.twohands.notification_service.unit.application.read;

import com.twohands.notification_service.application.read.ViewUserNotificationsCommand;
import com.twohands.notification_service.application.read.ViewUserNotificationsUseCase;
import com.twohands.notification_service.domain.common.PageResult;
import com.twohands.notification_service.domain.notificationevent.NotificationEventPayloadSanitizer;
import com.twohands.notification_service.domain.usernotification.NotificationDeliveryStatus;
import com.twohands.notification_service.domain.usernotification.UserNotification;
import com.twohands.notification_service.domain.usernotification.UserNotificationListQuery;
import com.twohands.notification_service.domain.usernotification.UserNotificationRepository;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViewUserNotificationsUseCaseTest {

    @Mock
    private UserNotificationRepository userNotificationRepository;

    @Mock
    private NotificationEventPayloadSanitizer metadataSanitizer;

    private ViewUserNotificationsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ViewUserNotificationsUseCase(
                userNotificationRepository,
                metadataSanitizer,
                new com.fasterxml.jackson.databind.ObjectMapper()
        );
    }

    @Test
    void execute_returnsPaginatedNotificationsForUser() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-05-24T10:00:00Z");

        when(userNotificationRepository.findVisibleByUserId(any(UserNotificationListQuery.class)))
                .thenReturn(new PageResult<>(
                        List.of(notification(userId, notificationId, createdAt, false, false)),
                        0,
                        20,
                        1,
                        1,
                        false
                ));
        when(metadataSanitizer.sanitize("{\"access_token\":\"secret\"}"))
                .thenReturn("{\"access_token\":\"***REDACTED***\"}");

        var result = useCase.execute(new ViewUserNotificationsCommand(userId, 0, 20));

        assertEquals(1, result.items().size());
        assertEquals(notificationId, result.items().getFirst().id());
        assertEquals("POST_LIKED", result.items().getFirst().type());
        assertFalse(result.items().getFirst().read());
        assertEquals(1, result.meta().totalElements());
        assertFalse(result.meta().hasNext());

        ArgumentCaptor<UserNotificationListQuery> captor = ArgumentCaptor.forClass(UserNotificationListQuery.class);
        verify(userNotificationRepository).findVisibleByUserId(captor.capture());
        assertEquals(userId, captor.getValue().userId());
        assertEquals(0, captor.getValue().page());
        assertEquals(20, captor.getValue().size());
    }

    @Test
    void execute_sanitizesMetadataBeforeReturning() {
        UUID userId = UUID.randomUUID();
        UserNotification notification = notification(
                userId,
                UUID.randomUUID(),
                Instant.now(),
                false,
                false
        );

        when(userNotificationRepository.findVisibleByUserId(any(UserNotificationListQuery.class)))
                .thenReturn(new PageResult<>(List.of(notification), 0, 20, 1, 1, false));
        when(metadataSanitizer.sanitize("{\"access_token\":\"secret\"}"))
                .thenReturn("{\"access_token\":\"***REDACTED***\"}");

        var result = useCase.execute(new ViewUserNotificationsCommand(userId, 0, 20));

        assertTrue(result.items().getFirst().metadata().contains("***REDACTED***"));
    }

    @Test
    void execute_throwsUnauthorizedWhenUserIdMissing() {
        AppException ex = assertThrows(AppException.class, () -> useCase.execute(
                new ViewUserNotificationsCommand(null, 0, 20)
        ));

        assertEquals(ErrorCode.UNAUTHORIZED, ex.getErrorCode());
    }

    @Test
    void execute_throwsForInvalidPage() {
        UUID userId = UUID.randomUUID();

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(
                new ViewUserNotificationsCommand(userId, -1, 20)
        ));

        assertEquals(ErrorCode.INVALID_PAGINATION, ex.getErrorCode());
    }

    @Test
    void execute_throwsForInvalidSize() {
        UUID userId = UUID.randomUUID();

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(
                new ViewUserNotificationsCommand(userId, 0, 51)
        ));

        assertEquals(ErrorCode.INVALID_PAGINATION, ex.getErrorCode());
    }

    private UserNotification notification(
            UUID userId,
            UUID id,
            Instant createdAt,
            boolean read,
            boolean deleted
    ) {
        return new UserNotification(
                id,
                UUID.randomUUID(),
                userId,
                UUID.randomUUID(),
                "POST_LIKED",
                "New like",
                "Someone liked your post.",
                "POST",
                "post-1",
                read,
                deleted,
                "{\"access_token\":\"secret\"}",
                NotificationDeliveryStatus.SENT,
                createdAt,
                read ? createdAt : null
        );
    }
}
