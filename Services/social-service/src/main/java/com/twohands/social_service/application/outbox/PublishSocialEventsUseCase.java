package com.twohands.social_service.application.outbox;

import com.twohands.social_service.domain.outbox.OutboxEvent;
import com.twohands.social_service.domain.outbox.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class PublishSocialEventsUseCase {

    private static final Logger log = LoggerFactory.getLogger(PublishSocialEventsUseCase.class);

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxEventPublisher outboxEventPublisher;
    private final int maxRetries;
    private final int batchSize;

    public PublishSocialEventsUseCase(
            OutboxEventRepository outboxEventRepository,
            OutboxEventPublisher outboxEventPublisher,
            @Value("${social.outbox.publish.max-retries:5}") int maxRetries,
            @Value("${social.outbox.publish.batch-size:50}") int batchSize
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.outboxEventPublisher = outboxEventPublisher;
        this.maxRetries = maxRetries;
        this.batchSize = batchSize;
    }

    @Transactional
    public int execute() {
        List<OutboxEvent> candidates = outboxEventRepository.claimPublishCandidates(batchSize, maxRetries);
        if (candidates.isEmpty()) {
            return 0;
        }

        Instant now = Instant.now();
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
                    "Outbox publish success. outboxEventId={}, eventType={}, aggregateId={}, newStatus=PUBLISHED",
                    event.id(),
                    event.eventType(),
                    event.aggregateId()
            );
        } catch (Exception ex) {
            outboxEventRepository.markFailed(event.id(), ex.getMessage());
            int nextRetryCount = event.retryCount() + 1;
            log.warn(
                    "Outbox publish failed. outboxEventId={}, eventType={}, aggregateId={}, retryCount={}, newStatus=FAILED, error={}",
                    event.id(),
                    event.eventType(),
                    event.aggregateId(),
                    nextRetryCount,
                    sanitizeError(ex.getMessage())
            );
            if (nextRetryCount >= maxRetries) {
                log.error(
                        "Outbox publish reached max retries. outboxEventId={}, eventType={}, retryCount={}, maxRetries={}",
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
