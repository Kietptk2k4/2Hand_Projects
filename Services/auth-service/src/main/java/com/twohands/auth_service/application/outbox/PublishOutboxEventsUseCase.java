package com.twohands.auth_service.application.outbox;

import com.twohands.auth_service.domain.outbox.OutboxEvent;
import com.twohands.auth_service.domain.outbox.OutboxEventRepository;
import com.twohands.auth_service.infrastructure.outbox.AuthOutboxTopicResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class PublishOutboxEventsUseCase {

    private static final Logger log = LoggerFactory.getLogger(PublishOutboxEventsUseCase.class);

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxEventPublisher outboxEventPublisher;
    private final AuthOutboxTopicResolver topicResolver;
    private final int maxRetries;
    private final int batchSize;

    public PublishOutboxEventsUseCase(
            OutboxEventRepository outboxEventRepository,
            OutboxEventPublisher outboxEventPublisher,
            AuthOutboxTopicResolver topicResolver,
            @Value("${auth.outbox.publish.max-retries:5}") int maxRetries,
            @Value("${auth.outbox.publish.batch-size:50}") int batchSize
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.outboxEventPublisher = outboxEventPublisher;
        this.topicResolver = topicResolver;
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
        String topic = resolveTopicSafely(event);
        try {
            outboxEventPublisher.publish(event);
            outboxEventRepository.markPublished(event.id(), now);
            log.info(
                    "Outbox publish success. outboxEventId={}, eventType={}, topic={}, source={}, newStatus=PUBLISHED",
                    event.id(),
                    event.eventType(),
                    topic,
                    event.source()
            );
        } catch (Exception ex) {
            outboxEventRepository.markFailed(event.id(), OutboxPublishErrorSanitizer.sanitize(ex.getMessage()));
            int nextRetryCount = event.retryCount() + 1;
            log.warn(
                    "Outbox publish failed. outboxEventId={}, eventType={}, topic={}, source={}, retryCount={}, newStatus=FAILED, error={}",
                    event.id(),
                    event.eventType(),
                    topic,
                    event.source(),
                    nextRetryCount,
                    OutboxPublishErrorSanitizer.sanitize(ex.getMessage())
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

    private String resolveTopicSafely(OutboxEvent event) {
        try {
            return topicResolver.resolve(event.eventType());
        } catch (Exception ex) {
            return "unknown";
        }
    }
}
