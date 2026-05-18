package com.twohands.auth_service.application.outbox;

import com.twohands.auth_service.domain.outbox.OutboxEvent;
import com.twohands.auth_service.domain.outbox.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class RetryFailedOutboxEventsUseCase {

    private static final Logger log = LoggerFactory.getLogger(RetryFailedOutboxEventsUseCase.class);

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxEventPublisher outboxEventPublisher;
    private final int maxRetries;
    private final int pendingTimeoutSeconds;
    private final int batchSize;

    public RetryFailedOutboxEventsUseCase(
            OutboxEventRepository outboxEventRepository,
            OutboxEventPublisher outboxEventPublisher,
            @Value("${auth.outbox.retry.max-retries:5}") int maxRetries,
            @Value("${auth.outbox.retry.pending-timeout-seconds:300}") int pendingTimeoutSeconds,
            @Value("${auth.outbox.retry.batch-size:50}") int batchSize
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.outboxEventPublisher = outboxEventPublisher;
        this.maxRetries = maxRetries;
        this.pendingTimeoutSeconds = pendingTimeoutSeconds;
        this.batchSize = batchSize;
    }

    @Transactional
    public int execute() {
        Instant now = Instant.now();
        Instant pendingTimeoutBefore = now.minusSeconds(pendingTimeoutSeconds);
        List<OutboxEvent> candidates = outboxEventRepository.claimRetryCandidates(batchSize, maxRetries, pendingTimeoutBefore);

        if (candidates.isEmpty()) {
            return 0;
        }

        for (OutboxEvent event : candidates) {
            processSingleEvent(event, now);
        }
        return candidates.size();
    }

    private void processSingleEvent(OutboxEvent event, Instant now) {
        try {
            outboxEventPublisher.publish(event);
            outboxEventRepository.markPublished(event.id(), now);
            log.info(
                    "Outbox retry publish success. outboxEventId={}, eventType={}, retryCount={}, newStatus=PUBLISHED",
                    event.id(),
                    event.eventType(),
                    event.retryCount()
            );
        } catch (Exception ex) {
            outboxEventRepository.markFailed(event.id(), ex.getMessage());
            int nextRetryCount = event.retryCount() + 1;
            log.warn(
                    "Outbox retry publish failed. outboxEventId={}, eventType={}, retryCount={}, newStatus=FAILED, error={}",
                    event.id(),
                    event.eventType(),
                    nextRetryCount,
                    sanitizeError(ex.getMessage())
            );
            if (nextRetryCount >= maxRetries) {
                log.error(
                        "Outbox retry reached max retries. outboxEventId={}, eventType={}, retryCount={}, maxRetries={}",
                        event.id(),
                        event.eventType(),
                        nextRetryCount,
                        maxRetries
                );
            }
        }
    }

    private String sanitizeError(String error) {
        if (error == null || error.isBlank()) {
            return "Unknown outbox publish error";
        }
        return error.length() > 500 ? error.substring(0, 500) : error;
    }
}
