package com.twohands.notification_service.application.read;

import com.twohands.notification_service.domain.usernotification.UserNotificationRepository;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class CountUnreadNotificationsUseCase {

    private final UserNotificationRepository userNotificationRepository;

    public CountUnreadNotificationsUseCase(UserNotificationRepository userNotificationRepository) {
        this.userNotificationRepository = userNotificationRepository;
    }

    public CountUnreadNotificationsResult execute(CountUnreadNotificationsCommand command) {
        validateCommand(command);

        long count = userNotificationRepository.countByUserIdAndReadFalseAndDeletedFalse(command.userId());
        return new CountUnreadNotificationsResult(count);
    }

    public String successMessage() {
        return "Unread notification count retrieved successfully";
    }

    private void validateCommand(CountUnreadNotificationsCommand command) {
        if (command.userId() == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
    }
}
