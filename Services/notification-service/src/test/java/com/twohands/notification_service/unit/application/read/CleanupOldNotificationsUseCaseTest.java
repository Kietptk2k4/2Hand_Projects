package com.twohands.notification_service.unit.application.read;

import com.twohands.notification_service.application.read.CleanupOldNotificationsUseCase;
import com.twohands.notification_service.domain.usernotification.NotificationRetentionPolicy;
import com.twohands.notification_service.domain.usernotification.UserNotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CleanupOldNotificationsUseCaseTest {

    @Mock
    private UserNotificationRepository userNotificationRepository;

    private CleanupOldNotificationsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CleanupOldNotificationsUseCase(userNotificationRepository, 180, 10);
    }

    @Test
    void execute_softDeletesEligibleNotificationsInBatches() {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();

        when(userNotificationRepository.findEligibleForRetentionCleanup(
                any(Instant.class),
                eq(NotificationRetentionPolicy.retainedNotificationTypes()),
                eq(NotificationRetentionPolicy.SYSTEM_ANNOUNCEMENT_REFERENCE_TYPE),
                eq(50)
        )).thenReturn(List.of(firstId, secondId))
                .thenReturn(List.of());

        when(userNotificationRepository.softDeleteByIds(List.of(firstId, secondId))).thenReturn(2);

        var result = useCase.execute(50);

        assertEquals(2, result.softDeletedCount());
        assertEquals(1, result.batchesProcessed());
        verify(userNotificationRepository).softDeleteByIds(List.of(firstId, secondId));
    }

    @Test
    void execute_returnsZeroWhenBatchSizeInvalid() {
        var result = useCase.execute(0);

        assertEquals(0, result.softDeletedCount());
        verify(userNotificationRepository, never()).findEligibleForRetentionCleanup(
                any(),
                any(),
                any(),
                anyInt()
        );
    }

    @Test
    void execute_failsSafeWhenRetentionDaysInvalid() {
        CleanupOldNotificationsUseCase invalidRetentionUseCase =
                new CleanupOldNotificationsUseCase(userNotificationRepository, 0, 10);

        var result = invalidRetentionUseCase.execute(50);

        assertEquals(0, result.softDeletedCount());
        verify(userNotificationRepository, never()).findEligibleForRetentionCleanup(
                any(),
                any(),
                any(),
                anyInt()
        );
    }
}
