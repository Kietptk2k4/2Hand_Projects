package com.twohands.admin_service.application.outbox;

import com.twohands.admin_service.domain.outbox.OutboxEvent;
import com.twohands.admin_service.domain.outbox.OutboxEventRepository;
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
	private final int maxRetries;
	private final int pendingTimeoutSeconds;
	private final int batchSize;

	public RetryAdminOutboxEventsUseCase(
			OutboxEventRepository outboxEventRepository,
			OutboxEventPublisher outboxEventPublisher,
			@Value("${admin.outbox.retry.max-retries:5}") int maxRetries,
			@Value("${admin.outbox.retry.pending-timeout-seconds:300}") int pendingTimeoutSeconds,
			@Value("${admin.outbox.retry.batch-size:50}") int batchSize
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
		List<OutboxEvent> candidates = outboxEventRepository.claimRetryCandidates(
				batchSize, maxRetries, pendingTimeoutBefore);

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
					"Outbox retry publish success. outboxEventId={}, eventType={}, aggregateId={}, retryCount={}, newStatus=PUBLISHED",
					event.id(),
					event.eventType(),
					event.aggregateId(),
					event.retryCount()
			);
		} catch (Exception ex) {
			outboxEventRepository.markFailed(event.id(), ex.getMessage());
			log.warn(
					"Outbox retry publish failed. outboxEventId={}, eventType={}, aggregateId={}, retryCount={}, newStatus=FAILED, error={}",
					event.id(),
					event.eventType(),
					event.aggregateId(),
					event.retryCount() + 1,
					sanitizeError(ex.getMessage())
			);
		}
	}

	private String sanitizeError(String error) {
		if (error == null || error.isBlank()) {
			return "Unknown outbox publish error";
		}
		return error.length() > 500 ? error.substring(0, 500) : error;
	}
}
