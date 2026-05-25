package com.twohands.notification_service.application.worker;

import com.twohands.notification_service.application.push.RetryFailedPushNotificationUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RetryFailedNotificationDeliveryUseCase {

    private static final Logger log = LoggerFactory.getLogger(RetryFailedNotificationDeliveryUseCase.class);

    private final RetryFailedPushNotificationUseCase retryFailedPushNotificationUseCase;

    public RetryFailedNotificationDeliveryUseCase(
            RetryFailedPushNotificationUseCase retryFailedPushNotificationUseCase
    ) {
        this.retryFailedPushNotificationUseCase = retryFailedPushNotificationUseCase;
    }

    public int execute(int batchSize) {
        int pushRetried = retryFailedPushNotificationUseCase.execute(batchSize);
        if (pushRetried > 0) {
            log.debug("Retried failed push deliveries. processedNotifications={}", pushRetried);
        }
        return pushRetried;
    }
}
