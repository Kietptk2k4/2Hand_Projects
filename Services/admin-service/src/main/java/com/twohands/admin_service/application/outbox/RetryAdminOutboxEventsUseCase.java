package com.twohands.admin_service.application.outbox;

import com.twohands.admin_service.domain.outbox.OutboxEvent;
import com.twohands.admin_service.domain.outbox.OutboxEventRepository;
import com.twohands.admin_service.domain.outbox.OutboxRetryPolicy;
import com.twohands.admin_service.infrastructure.outbox.AdminOutboxTopicResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class RetryAdminOutboxEventsUseCase {

	private static final Logger log = LoggerFactory.getLogger(RetryAdminOutboxEventsUseCase.class);

	private final OutboxEventRepository outboxEventRepository;
	private final OutboxEventPublisher outboxEventPublisher;
	private final AdminOutboxTopicResolver topicResolver;
	private final int maxRetries;
	private final int pendingTimeoutSeconds;
	private final int batchSize;
	private final int backoffSecondsPerAttempt;

	public RetryAdminOutboxEventsUseCase(
			OutboxEventRepository outboxEventRepository,
			OutboxEventPublisher outboxEventPublisher,
			AdminOutboxTopicResolver topicResolver,
			@Value("${admin.outbox.retry.max-retries:5}") int maxRetries,
			@Value("${admin.outbox.retry.pending-timeout-seconds:300}") int pendingTimeoutSeconds,
			@Value("${admin.outbox.retry.batch-size:50}") int batchSize,
			@Value("${admin.outbox.retry.backoff-seconds-per-attempt:60}") int backoffSecondsPerAttempt
	) {
		this.outboxEventRepository = outboxEventRepository;
		this.outboxEventPublisher = outboxEventPublisher;
		this.topicResolver = topicResolver;
		this.maxRetries = maxRetries;
		this.pendingTimeoutSeconds = pendingTimeoutSeconds;
		this.batchSize = batchSize;
		this.backoffSecondsPerAttempt = backoffSecondsPerAttempt;
	}

	@Transactional
	public int execute() {
		Instant now = Instant.now();
		Instant pendingTimeoutBefore = now.minusSeconds(pendingTimeoutSeconds);
		List<OutboxEvent> candidates = outboxEventRepository.claimRetryCandidates(
				batchSize, maxRetries, pendingTimeoutBefore);

		if (candidates.isEmpty()) {
			return 0;
		}

		int processed = 0;
		for (OutboxEvent event : candidates) {
			if (!OutboxRetryPolicy.shouldAttemptRetry(event, now, backoffSecondsPerAttempt)) {
				skipRetryCandidate(event);
				continue;
			}
			processSingleEvent(event, now);
			processed++;
		}
		return processed;
	}

	private void skipRetryCandidate(OutboxEvent event) {
		outboxEventRepository.revertProcessingToFailed(event.id());
		String topic = resolveTopicSafely(event);
		if (!OutboxRetryPolicy.isRetryableLastError(event.lastError())) {
			log.warn(
					"Outbox retry skipped (non-retryable error). outboxEventId={}, eventType={}, topic={}, aggregateId={}, lastError={}",
					event.id(),
					event.eventType(),
					topic,
					event.aggregateId(),
					sanitizeError(event.lastError())
			);
			return;
		}
		log.debug(
				"Outbox retry skipped (backoff not elapsed). outboxEventId={}, eventType={}, topic={}, retryCount={}, createdAt={}",
				event.id(),
				event.eventType(),
				topic,
				event.retryCount(),
				event.createdAt()
		);
	}

	private void processSingleEvent(OutboxEvent event, Instant now) {
		String topic = resolveTopicSafely(event);
		try {
			outboxEventPublisher.publish(event);
			outboxEventRepository.markPublished(event.id(), now);
			log.info(
					"Outbox retry publish success. outboxEventId={}, eventType={}, topic={}, aggregateId={}, retryCount={}, newStatus=PUBLISHED",
					event.id(),
					event.eventType(),
					topic,
					event.aggregateId(),
					event.retryCount()
			);
		} catch (Exception ex) {
			outboxEventRepository.markFailed(event.id(), ex.getMessage());
			int nextRetryCount = event.retryCount() + 1;
			log.warn(
					"Outbox retry publish failed. outboxEventId={}, eventType={}, topic={}, aggregateId={}, retryCount={}, newStatus=FAILED, error={}",
					event.id(),
					event.eventType(),
					topic,
					event.aggregateId(),
					nextRetryCount,
					sanitizeError(ex.getMessage())
			);
			if (nextRetryCount >= maxRetries) {
				log.error(
						"Outbox retry reached max retries. outboxEventId={}, eventType={}, topic={}, retryCount={}, maxRetries={}",
						event.id(),
						event.eventType(),
						topic,
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

	private String sanitizeError(String error) {
		if (error == null || error.isBlank()) {
			return "Unknown outbox publish error";
		}
		return error.length() > 500 ? error.substring(0, 500) : error;
	}
}
