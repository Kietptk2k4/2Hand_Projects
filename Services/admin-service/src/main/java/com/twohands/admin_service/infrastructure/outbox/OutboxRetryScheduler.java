package com.twohands.admin_service.infrastructure.outbox;

import com.twohands.admin_service.application.outbox.RetryAdminOutboxEventsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxRetryScheduler {

	private static final Logger log = LoggerFactory.getLogger(OutboxRetryScheduler.class);

	private final RetryAdminOutboxEventsUseCase retryAdminOutboxEventsUseCase;
	private final boolean enabled;

	public OutboxRetryScheduler(
			RetryAdminOutboxEventsUseCase retryAdminOutboxEventsUseCase,
			@Value("${admin.outbox.retry.enabled:false}") boolean enabled
	) {
		this.retryAdminOutboxEventsUseCase = retryAdminOutboxEventsUseCase;
		this.enabled = enabled;
	}

	@Scheduled(cron = "${admin.outbox.retry.cron:0 */1 * * * *}")
	public void runRetryJob() {
		if (!enabled) {
			return;
		}
		int processed = retryAdminOutboxEventsUseCase.execute();
		if (processed > 0) {
			log.info("Outbox retry job completed. processedEvents={}", processed);
		}
	}
}
