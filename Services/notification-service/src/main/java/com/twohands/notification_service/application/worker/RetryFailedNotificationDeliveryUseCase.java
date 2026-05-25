package com.twohands.notification_service.application.worker;

import com.twohands.notification_service.application.email.RetryFailedEmailNotificationUseCase;
import com.twohands.notification_service.application.push.RetryFailedPushNotificationUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RetryFailedNotificationDeliveryUseCase {

    private static final Logger log = LoggerFactory.getLogger(RetryFailedNotificationDeliveryUseCase.class);

    private final RetryFailedPushNotificationUseCase retryFailedPushNotificationUseCase;
    private final RetryFailedEmailNotificationUseCase retryFailedEmailNotificationUseCase;

    public RetryFailedNotificationDeliveryUseCase(
            RetryFailedPushNotificationUseCase retryFailedPushNotificationUseCase,
            RetryFailedEmailNotificationUseCase retryFailedEmailNotificationUseCase
    ) {
        this.retryFailedPushNotificationUseCase = retryFailedPushNotificationUseCase;
        this.retryFailedEmailNotificationUseCase = retryFailedEmailNotificationUseCase;
    }

    public int execute(int batchSize) {
        int pushRetried = retryFailedPushNotificationUseCase.execute(batchSize);
        int emailRetried = retryFailedEmailNotificationUseCase.execute(batchSize);

        if (pushRetried > 0) {
            log.debug("Retried failed push deliveries. processedNotifications={}", pushRetried);
        }
        if (emailRetried > 0) {
            log.debug("Retried failed email deliveries. processedNotifications={}", emailRetried);
        }
        return pushRetried + emailRetried;
    }
}
