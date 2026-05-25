package com.twohands.notification_service.infrastructure.scheduler;

import com.twohands.notification_service.application.devicetoken.CleanupInvalidDeviceTokenUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CleanupInvalidDeviceTokenScheduler {

    private static final Logger log = LoggerFactory.getLogger(CleanupInvalidDeviceTokenScheduler.class);

    private final CleanupInvalidDeviceTokenUseCase cleanupInvalidDeviceTokenUseCase;
    private final boolean enabled;
    private final int batchSize;

    public CleanupInvalidDeviceTokenScheduler(
            CleanupInvalidDeviceTokenUseCase cleanupInvalidDeviceTokenUseCase,
            @Value("${notification.workers.cleanup-invalid-device-tokens.enabled:false}") boolean enabled,
            @Value("${notification.workers.cleanup-invalid-device-tokens.batch-size:100}") int batchSize
    ) {
        this.cleanupInvalidDeviceTokenUseCase = cleanupInvalidDeviceTokenUseCase;
        this.enabled = enabled;
        this.batchSize = batchSize;
    }

    @Scheduled(cron = "${notification.workers.cleanup-invalid-device-tokens.cron:0 0 3 * * *}")
    public void run() {
        if (!enabled) {
            return;
        }
        int deactivated = cleanupInvalidDeviceTokenUseCase.execute(batchSize);
        if (deactivated > 0) {
            log.info("Cleanup invalid device tokens job completed. deactivatedTokens={}", deactivated);
        }
    }
}
