package com.twohands.notification_service.unit.application.delivery;

import com.twohands.notification_service.application.delivery.ApplySkipSelfNotificationCommand;
import com.twohands.notification_service.application.delivery.ApplySkipSelfNotificationUseCase;
import com.twohands.notification_service.domain.delivery.SkipSelfNotificationOutcome;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApplySkipSelfNotificationUseCaseTest {

    private ApplySkipSelfNotificationUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ApplySkipSelfNotificationUseCase();
    }

    @Test
    void execute_returnsSkipForSelfSocialInteraction() {
        UUID userId = UUID.randomUUID();

        SkipSelfNotificationOutcome outcome = useCase.execute(new ApplySkipSelfNotificationCommand(
                "POST_LIKED",
                NotificationSourceService.SOCIAL,
                userId,
                userId
        ));

        assertEquals(SkipSelfNotificationOutcome.SKIP, outcome);
    }

    @Test
    void execute_returnsMissingActorForSelfSkipEventWithoutActor() {
        SkipSelfNotificationOutcome outcome = useCase.execute(new ApplySkipSelfNotificationCommand(
                "COMMENT_LIKED",
                NotificationSourceService.SOCIAL,
                null,
                UUID.randomUUID()
        ));

        assertEquals(SkipSelfNotificationOutcome.MISSING_ACTOR, outcome);
    }

    @Test
    void execute_throwsWhenRecipientMissing() {
        AppException ex = assertThrows(AppException.class, () -> useCase.execute(
                new ApplySkipSelfNotificationCommand(
                        "POST_LIKED",
                        NotificationSourceService.SOCIAL,
                        UUID.randomUUID(),
                        null
                )
        ));

        assertEquals(ErrorCode.VALIDATION_ERROR, ex.getErrorCode());
    }
}
