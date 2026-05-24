package com.twohands.notification_service.unit.application.read;

import com.twohands.notification_service.application.read.MarkNotificationAsReadCommand;
import com.twohands.notification_service.application.read.MarkNotificationAsReadUseCase;
import com.twohands.notification_service.domain.usernotification.NotificationDeliveryStatus;
import com.twohands.notification_service.domain.usernotification.UserNotification;
import com.twohands.notification_service.domain.usernotification.UserNotificationRepository;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class MarkNotificationAsReadUseCaseTest {

    @Mock
    private UserNotificationRepository userNotificationRepository;

    private MarkNotificationAsReadUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new MarkNotificationAsReadUseCase(userNotificationRepository);
    }

    @Test
    void execute_marksUnreadNotificationAsRead() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        UserNotification unread = notification(notificationId, userId, false, null);

        when(userNotificationRepository.findVisibleByIdAndUserId(notificationId, userId))
                .thenReturn(Optional.of(unread));
        when(userNotificationRepository.save(any(UserNotification.class))).thenAnswer(invocation -> {
            UserNotification saved = invocation.getArgument(0);
            return new UserNotification(
                    saved.id(),
                    saved.notificationEventId(),
                    saved.userId(),
                    saved.actorId(),
                    saved.type(),
                    saved.title(),
                    saved.content(),
                    saved.referenceType(),
                    saved.referenceId(),
                    saved.read(),
                    saved.deleted(),
                    saved.metadata(),
                    saved.deliveryStatus(),
                    saved.createdAt(),
                    saved.readAt()
            );
        });

        var result = useCase.execute(new MarkNotificationAsReadCommand(userId, notificationId));

        assertTrue(result.read());
        assertFalse(result.alreadyRead());
        assertEquals(notificationId, result.notificationId());
        verify(userNotificationRepository).save(any(UserNotification.class));
    }

    @Test
    void execute_returnsSuccessWithoutUpdateWhenAlreadyRead() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        Instant readAt = Instant.parse("2026-05-24T11:00:00Z");
        UserNotification read = notification(notificationId, userId, true, readAt);

        when(userNotificationRepository.findVisibleByIdAndUserId(notificationId, userId))
                .thenReturn(Optional.of(read));

        var result = useCase.execute(new MarkNotificationAsReadCommand(userId, notificationId));

        assertTrue(result.read());
        assertTrue(result.alreadyRead());
        assertEquals(readAt, result.readAt());
        verify(userNotificationRepository, never()).save(any());
    }

    @Test
    void execute_throwsNotFoundWhenNotificationMissingOrDeleted() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        when(userNotificationRepository.findVisibleByIdAndUserId(notificationId, userId))
                .thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(
                new MarkNotificationAsReadCommand(userId, notificationId)
        ));

        assertEquals(ErrorCode.USER_NOTIFICATION_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void execute_throwsUnauthorizedWhenUserIdMissing() {
        AppException ex = assertThrows(AppException.class, () -> useCase.execute(
                new MarkNotificationAsReadCommand(null, UUID.randomUUID())
        ));

        assertEquals(ErrorCode.UNAUTHORIZED, ex.getErrorCode());
    }

    private UserNotification notification(UUID id, UUID userId, boolean read, Instant readAt) {
        return new UserNotification(
                id,
                UUID.randomUUID(),
                userId,
                UUID.randomUUID(),
                "POST_LIKED",
                "Title",
                "Content",
                "POST",
                "post-1",
                read,
                false,
                "{}",
                NotificationDeliveryStatus.SENT,
                Instant.now(),
                readAt
        );
    }
}
