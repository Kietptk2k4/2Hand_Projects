package com.twohands.notification_service.unit.application.read;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.read.DismissAnnouncementNotificationCommand;
import com.twohands.notification_service.application.read.DismissAnnouncementNotificationUseCase;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DismissAnnouncementNotificationUseCaseTest {

    @Mock
    private UserNotificationRepository userNotificationRepository;

    private DismissAnnouncementNotificationUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DismissAnnouncementNotificationUseCase(
                userNotificationRepository,
                new ObjectMapper()
        );
    }

    @Test
    void execute_softDeletesDismissibleAnnouncement() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        UserNotification notification = announcement(notificationId, userId, false, true);

        when(userNotificationRepository.findByIdAndUserId(notificationId, userId))
                .thenReturn(Optional.of(notification));
        when(userNotificationRepository.save(any(UserNotification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(new DismissAnnouncementNotificationCommand(userId, notificationId));

        assertTrue(result.dismissed());
        assertEquals(notificationId, result.notificationId());
        verify(userNotificationRepository).save(any(UserNotification.class));
    }

    @Test
    void execute_returnsIdempotentWhenAlreadyDismissed() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        UserNotification notification = announcement(notificationId, userId, true, true);

        when(userNotificationRepository.findByIdAndUserId(notificationId, userId))
                .thenReturn(Optional.of(notification));

        var result = useCase.execute(new DismissAnnouncementNotificationCommand(userId, notificationId));

        assertTrue(result.dismissed());
        assertTrue(result.alreadyDismissed());
        verify(userNotificationRepository, never()).save(any());
    }

    @Test
    void execute_throwsConflictWhenNotDismissible() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        when(userNotificationRepository.findByIdAndUserId(notificationId, userId))
                .thenReturn(Optional.of(announcement(notificationId, userId, false, false)));

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(
                new DismissAnnouncementNotificationCommand(userId, notificationId)
        ));

        assertEquals(ErrorCode.ANNOUNCEMENT_NOT_DISMISSIBLE, ex.getErrorCode());
    }

    @Test
    void execute_throwsBadRequestWhenNotSystemAnnouncement() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        UserNotification notification = new UserNotification(
                notificationId,
                UUID.randomUUID(),
                userId,
                null,
                "POST_LIKED",
                "Title",
                "Content",
                "POST",
                "post-1",
                false,
                false,
                "{\"dismissible\":true}",
                NotificationDeliveryStatus.SENT,
                Instant.now(),
                null
        );

        when(userNotificationRepository.findByIdAndUserId(notificationId, userId))
                .thenReturn(Optional.of(notification));

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(
                new DismissAnnouncementNotificationCommand(userId, notificationId)
        ));

        assertEquals(ErrorCode.NOT_SYSTEM_ANNOUNCEMENT, ex.getErrorCode());
    }

    @Test
    void execute_throwsNotFoundWhenMissing() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        when(userNotificationRepository.findByIdAndUserId(notificationId, userId))
                .thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(
                new DismissAnnouncementNotificationCommand(userId, notificationId)
        ));

        assertEquals(ErrorCode.USER_NOTIFICATION_NOT_FOUND, ex.getErrorCode());
    }

    private UserNotification announcement(UUID id, UUID userId, boolean deleted, boolean dismissible) {
        return new UserNotification(
                id,
                UUID.randomUUID(),
                userId,
                null,
                "SYSTEM_ANNOUNCEMENT_SENT",
                "Announcement",
                "Body",
                "SYSTEM_ANNOUNCEMENT",
                "ann-1",
                false,
                deleted,
                "{\"dismissible\":" + dismissible + "}",
                NotificationDeliveryStatus.SENT,
                Instant.now(),
                null
        );
    }
}
