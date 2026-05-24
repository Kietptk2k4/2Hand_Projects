package com.twohands.notification_service.application.read;

import com.twohands.notification_service.domain.usernotification.UserNotificationRepository;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class MarkAllNotificationsAsReadUseCase {

    private final UserNotificationRepository userNotificationRepository;

    public MarkAllNotificationsAsReadUseCase(UserNotificationRepository userNotificationRepository) {
        this.userNotificationRepository = userNotificationRepository;
    }

    @Transactional
    public MarkAllNotificationsAsReadResult execute(MarkAllNotificationsAsReadCommand command) {
        validateCommand(command);

        int updatedCount = userNotificationRepository.markAllUnreadVisibleAsRead(
                command.userId(),
                Instant.now()
        );

        return new MarkAllNotificationsAsReadResult(updatedCount);
    }

    public String successMessage() {
        return "All notifications marked as read";
    }

    private void validateCommand(MarkAllNotificationsAsReadCommand command) {
        if (command.userId() == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
    }
}
