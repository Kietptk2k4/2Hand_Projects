package com.twohands.notification_service.application.read;

import com.twohands.notification_service.domain.usernotification.NotificationRetentionPolicy;
import com.twohands.notification_service.domain.usernotification.UserNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class CleanupOldNotificationsUseCase {

    private static final Logger log = LoggerFactory.getLogger(CleanupOldNotificationsUseCase.class);

    private final UserNotificationRepository userNotificationRepository;
    private final int retentionDays;
    private final int maxBatchesPerRun;

    public CleanupOldNotificationsUseCase(
            UserNotificationRepository userNotificationRepository,
            @Value("${notification.workers.cleanup-old-notifications.retention-days:180}") int retentionDays,
            @Value("${notification.workers.cleanup-old-notifications.max-batches-per-run:100}") int maxBatchesPerRun
    ) {
        this.userNotificationRepository = userNotificationRepository;
        this.retentionDays = retentionDays;
        this.maxBatchesPerRun = Math.max(maxBatchesPerRun, 1);
    }

    @Transactional
    public CleanupOldNotificationsResult execute(int batchSize) {
        if (batchSize <= 0) {
            return new CleanupOldNotificationsResult(0, 0);
        }
        if (!NotificationRetentionPolicy.isRetentionDaysValid(retentionDays)) {
            log.warn(
                    "Skipping old notification cleanup because retention-days is invalid. retentionDays={}",
                    retentionDays
            );
            return new CleanupOldNotificationsResult(0, 0);
        }

        Instant createdBefore = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        int totalSoftDeleted = 0;
        int batchesProcessed = 0;

        while (batchesProcessed < maxBatchesPerRun) {
            List<UUID> candidateIds = userNotificationRepository.findEligibleForRetentionCleanup(
                    createdBefore,
                    NotificationRetentionPolicy.retainedNotificationTypes(),
                    NotificationRetentionPolicy.SYSTEM_ANNOUNCEMENT_REFERENCE_TYPE,
                    batchSize
            );
            if (candidateIds.isEmpty()) {
                break;
            }

            int softDeleted = userNotificationRepository.softDeleteByIds(candidateIds);
            totalSoftDeleted += softDeleted;
            batchesProcessed++;

            if (candidateIds.size() < batchSize) {
                break;
            }
        }

        if (totalSoftDeleted > 0) {
            log.info(
                    "Cleanup old notifications completed. softDeletedCount={} batchesProcessed={} retentionDays={}",
                    totalSoftDeleted,
                    batchesProcessed,
                    retentionDays
            );
        }
        return new CleanupOldNotificationsResult(totalSoftDeleted, batchesProcessed);
    }
}
