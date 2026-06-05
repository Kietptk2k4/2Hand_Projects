package com.twohands.notification_service.application.announcement;

import com.twohands.notification_service.domain.usernotification.DismissSystemAnnouncementNotificationPolicy;
import com.twohands.notification_service.domain.usernotification.UserNotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WithdrawSystemAnnouncementNotificationsUseCase {

    private final UserNotificationRepository userNotificationRepository;

    public WithdrawSystemAnnouncementNotificationsUseCase(UserNotificationRepository userNotificationRepository) {
        this.userNotificationRepository = userNotificationRepository;
    }

    @Transactional
    public int execute(String announcementId) {
        if (announcementId == null || announcementId.isBlank()) {
            return 0;
        }
        return userNotificationRepository.softDeleteVisibleByReference(
                DismissSystemAnnouncementNotificationPolicy.SYSTEM_ANNOUNCEMENT_REFERENCE_TYPE,
                announcementId.trim()
        );
    }
}
