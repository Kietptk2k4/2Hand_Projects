package com.twohands.notification_service.application.worker;

import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProcessPendingNotificationEventsUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessPendingNotificationEventsUseCase.class);
    private static final String WORKER_ID = "notification-processor";

    private final NotificationEventRepository notificationEventRepository;
    private final ProcessNotificationEventUseCase processNotificationEventUseCase;

    public ProcessPendingNotificationEventsUseCase(
            NotificationEventRepository notificationEventRepository,
            ProcessNotificationEventUseCase processNotificationEventUseCase
    ) {
        this.notificationEventRepository = notificationEventRepository;
        this.processNotificationEventUseCase = processNotificationEventUseCase;
    }

    public int execute(int batchSize) {
        if (batchSize <= 0) {
            return 0;
        }

        List<NotificationEvent> claimedEvents = notificationEventRepository.claimProcessableEvents(batchSize, WORKER_ID);
        if (claimedEvents.isEmpty()) {
            return 0;
        }

        int processed = 0;
        for (NotificationEvent event : claimedEvents) {
            ProcessNotificationEventResult result = processNotificationEventUseCase.execute(
                    new ProcessNotificationEventCommand(event.id())
            );
            if (result.outcome() != ProcessNotificationEventOutcome.SKIPPED) {
                processed++;
            }
            log.debug(
                    "Processed notification event. eventId={}, outcome={}",
                    event.id(),
                    result.outcome()
            );
        }
        return processed;
    }
}
