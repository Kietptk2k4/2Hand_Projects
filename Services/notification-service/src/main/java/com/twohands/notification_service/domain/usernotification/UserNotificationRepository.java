package com.twohands.notification_service.domain.usernotification;

import com.twohands.notification_service.domain.common.PageResult;
import com.twohands.notification_service.domain.idempotency.UserNotificationIdempotencyKey;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserNotificationRepository {

    UserNotification save(UserNotification notification);

    Optional<UserNotification> findById(UUID notificationId);

    List<UserNotification> findFailedDeliveryCandidates(int batchSize);

    Optional<UserNotification> findByIdempotencyKey(UserNotificationIdempotencyKey idempotencyKey);

    Optional<UserNotification> findVisibleByIdAndUserId(UUID notificationId, UUID userId);

    Optional<UserNotification> findByIdAndUserId(UUID notificationId, UUID userId);

    PageResult<UserNotification> findVisibleByUserId(UserNotificationListQuery query);

    PageResult<UserNotification> findUnreadVisibleByUserId(UserNotificationListQuery query);

    long countByUserIdAndReadFalseAndDeletedFalse(UUID userId);

    int markAllUnreadVisibleAsRead(UUID userId, Instant readAt);
}
