package com.twohands.notification_service.unit.application.read;

import com.twohands.notification_service.application.read.CountUnreadNotificationsCommand;
import com.twohands.notification_service.application.read.CountUnreadNotificationsUseCase;
import com.twohands.notification_service.domain.usernotification.UserNotificationRepository;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CountUnreadNotificationsUseCaseTest {

    @Mock
    private UserNotificationRepository userNotificationRepository;

    private CountUnreadNotificationsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CountUnreadNotificationsUseCase(userNotificationRepository);
    }

    @Test
    void execute_returnsUnreadCountForUser() {
        UUID userId = UUID.randomUUID();
        when(userNotificationRepository.countByUserIdAndReadFalseAndDeletedFalse(userId)).thenReturn(3L);

        var result = useCase.execute(new CountUnreadNotificationsCommand(userId));

        assertEquals(3L, result.count());
        verify(userNotificationRepository).countByUserIdAndReadFalseAndDeletedFalse(userId);
    }

    @Test
    void execute_returnsZeroWhenNoUnreadNotifications() {
        UUID userId = UUID.randomUUID();
        when(userNotificationRepository.countByUserIdAndReadFalseAndDeletedFalse(userId)).thenReturn(0L);

        var result = useCase.execute(new CountUnreadNotificationsCommand(userId));

        assertEquals(0L, result.count());
    }

    @Test
    void execute_throwsUnauthorizedWhenUserIdMissing() {
        AppException ex = assertThrows(AppException.class, () -> useCase.execute(
                new CountUnreadNotificationsCommand(null)
        ));

        assertEquals(ErrorCode.UNAUTHORIZED, ex.getErrorCode());
    }
}
