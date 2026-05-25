package com.twohands.notification_service.application.read;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.domain.common.PageResult;
import com.twohands.notification_service.domain.notificationevent.NotificationEventPayloadSanitizer;
import com.twohands.notification_service.domain.usernotification.UserNotification;
import com.twohands.notification_service.domain.usernotification.UserNotificationListQuery;
import com.twohands.notification_service.domain.usernotification.UserNotificationRepository;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ViewUnreadNotificationsUseCase {

    private static final int MIN_PAGE = 0;
    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 50;

    private final UserNotificationRepository userNotificationRepository;
    private final NotificationEventPayloadSanitizer metadataSanitizer;
    private final ObjectMapper objectMapper;

    public ViewUnreadNotificationsUseCase(
            UserNotificationRepository userNotificationRepository,
            NotificationEventPayloadSanitizer metadataSanitizer,
            ObjectMapper objectMapper
    ) {
        this.userNotificationRepository = userNotificationRepository;
        this.metadataSanitizer = metadataSanitizer;
        this.objectMapper = objectMapper;
    }

    public ViewUserNotificationsResult execute(ViewUnreadNotificationsCommand command) {
        validateCommand(command);

        PageResult<UserNotification> page = userNotificationRepository.findUnreadVisibleByUserId(
                new UserNotificationListQuery(command.userId(), command.page(), command.size())
        );

        List<ViewUserNotificationsResult.UserNotificationItem> items = page.items().stream()
                .map(this::toItem)
                .toList();

        return ViewUserNotificationsResult.from(new PageResult<>(
                items,
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages(),
                page.hasNext()
        ));
    }

    public String successMessage() {
        return "Unread notifications retrieved successfully";
    }

    private ViewUserNotificationsResult.UserNotificationItem toItem(UserNotification notification) {
        return new ViewUserNotificationsResult.UserNotificationItem(
                notification.id(),
                notification.actorId(),
                notification.type(),
                notification.title(),
                notification.content(),
                notification.referenceType(),
                notification.referenceId(),
                UserNotificationMetadataPresenter.present(
                        objectMapper,
                        metadataSanitizer,
                        notification.referenceType(),
                        notification.metadata()
                ),
                notification.read(),
                notification.readAt(),
                notification.createdAt()
        );
    }

    private void validateCommand(ViewUnreadNotificationsCommand command) {
        if (command.userId() == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
        validatePagination(command.page(), command.size());
    }

    private void validatePagination(int page, int size) {
        if (page < MIN_PAGE) {
            throw new AppException(
                    ErrorCode.INVALID_PAGINATION,
                    "Invalid pagination parameters",
                    "page",
                    "MUST_BE_GREATER_THAN_OR_EQUAL_TO_0"
            );
        }
        if (size < MIN_SIZE || size > MAX_SIZE) {
            throw new AppException(
                    ErrorCode.INVALID_PAGINATION,
                    "Invalid pagination parameters",
                    "size",
                    "MUST_BE_BETWEEN_1_AND_50"
            );
        }
    }
}
