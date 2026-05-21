package com.twohands.notification_service.application.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RetryFailedNotificationEventsUseCase {

    private static final Logger log = LoggerFactory.getLogger(RetryFailedNotificationEventsUseCase.class);

    public int execute(int batchSize) {
        log.debug("Retry failed notification events stub. batchSize={}", batchSize);
        return 0;
    }
}
