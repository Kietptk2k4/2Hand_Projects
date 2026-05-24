package com.twohands.notification_service.application.read;

import com.twohands.notification_service.domain.usernotification.MarkNotificationAsReadOutcome;
import com.twohands.notification_service.domain.usernotification.MarkNotificationAsReadPolicy;
import com.twohands.notification_service.domain.usernotification.UserNotification;
import com.twohands.notification_service.domain.usernotification.UserNotificationRepository;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class MarkNotificationAsReadUseCase {

    private final UserNotificationRepository userNotificationRepository;

    public MarkNotificationAsReadUseCase(UserNotificationRepository userNotificationRepository) {
        this.userNotificationRepository = userNotificationRepository;
    }

    @Transactional
    public MarkNotificationAsReadResult execute(MarkNotificationAsReadCommand command) {
        validateCommand(command);

        UserNotification notification = userNotificationRepository.findVisibleByIdAndUserId(
                command.notificationId(),
                command.userId()
        ).orElseThrow(() -> new AppException(
                ErrorCode.USER_NOTIFICATION_NOT_FOUND,
                "User notification not found"
        ));

        MarkNotificationAsReadOutcome outcome = MarkNotificationAsReadPolicy.apply(notification, Instant.now());
        UserNotification saved = outcome.changed()
                ? userNotificationRepository.save(outcome.notification())
                : outcome.notification();

        return new MarkNotificationAsReadResult(
                saved.id(),
                saved.read(),
                saved.readAt(),
                !outcome.changed()
        );
    }

    public String successMessage() {
        return "Notification marked as read";
    }

    private void validateCommand(MarkNotificationAsReadCommand command) {
        if (command.userId() == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
        if (command.notificationId() == null) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "notificationId",
                    "Notification id is required."
            );
        }
    }
}
