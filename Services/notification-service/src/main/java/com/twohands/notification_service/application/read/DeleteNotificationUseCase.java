package com.twohands.notification_service.application.read;

import com.twohands.notification_service.domain.usernotification.SoftDeleteNotificationOutcome;
import com.twohands.notification_service.domain.usernotification.SoftDeleteNotificationPolicy;
import com.twohands.notification_service.domain.usernotification.UserNotification;
import com.twohands.notification_service.domain.usernotification.UserNotificationRepository;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteNotificationUseCase {

    private final UserNotificationRepository userNotificationRepository;

    public DeleteNotificationUseCase(UserNotificationRepository userNotificationRepository) {
        this.userNotificationRepository = userNotificationRepository;
    }

    @Transactional
    public DeleteNotificationResult execute(DeleteNotificationCommand command) {
        validateCommand(command);

        UserNotification notification = userNotificationRepository.findByIdAndUserId(
                command.notificationId(),
                command.userId()
        ).orElseThrow(() -> new AppException(
                ErrorCode.USER_NOTIFICATION_NOT_FOUND,
                "User notification not found"
        ));

        SoftDeleteNotificationOutcome outcome = SoftDeleteNotificationPolicy.apply(notification);
        UserNotification saved = outcome.changed()
                ? userNotificationRepository.save(outcome.notification())
                : outcome.notification();

        return new DeleteNotificationResult(
                saved.id(),
                saved.deleted(),
                !outcome.changed()
        );
    }

    public String successMessage() {
        return "Notification deleted successfully";
    }

    private void validateCommand(DeleteNotificationCommand command) {
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
