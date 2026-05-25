package com.twohands.notification_service.application.read;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.domain.usernotification.DismissSystemAnnouncementNotificationPolicy;
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
public class DismissAnnouncementNotificationUseCase {

    private final UserNotificationRepository userNotificationRepository;
    private final ObjectMapper objectMapper;

    public DismissAnnouncementNotificationUseCase(
            UserNotificationRepository userNotificationRepository,
            ObjectMapper objectMapper
    ) {
        this.userNotificationRepository = userNotificationRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public DismissAnnouncementNotificationResult execute(DismissAnnouncementNotificationCommand command) {
        validateCommand(command);

        UserNotification notification = userNotificationRepository.findByIdAndUserId(
                command.notificationId(),
                command.userId()
        ).orElseThrow(() -> new AppException(
                ErrorCode.USER_NOTIFICATION_NOT_FOUND,
                "User notification not found"
        ));

        if (!DismissSystemAnnouncementNotificationPolicy.isSystemAnnouncement(notification)) {
            throw new AppException(
                    ErrorCode.NOT_SYSTEM_ANNOUNCEMENT,
                    "Only system announcement notifications can be dismissed",
                    "referenceType",
                    "Notification reference type must be SYSTEM_ANNOUNCEMENT."
            );
        }

        if (notification.deleted()) {
            return new DismissAnnouncementNotificationResult(
                    notification.id(),
                    true,
                    true
            );
        }

        if (!DismissSystemAnnouncementNotificationPolicy.isDismissible(notification, objectMapper)) {
            throw new AppException(
                    ErrorCode.ANNOUNCEMENT_NOT_DISMISSIBLE,
                    "This system announcement cannot be dismissed",
                    "metadata",
                    "metadata.dismissible must be true."
            );
        }

        SoftDeleteNotificationOutcome outcome = SoftDeleteNotificationPolicy.apply(notification);
        UserNotification saved = outcome.changed()
                ? userNotificationRepository.save(outcome.notification())
                : outcome.notification();

        return new DismissAnnouncementNotificationResult(
                saved.id(),
                saved.deleted(),
                !outcome.changed()
        );
    }

    public String successMessage() {
        return "Announcement notification dismissed successfully";
    }

    private void validateCommand(DismissAnnouncementNotificationCommand command) {
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
