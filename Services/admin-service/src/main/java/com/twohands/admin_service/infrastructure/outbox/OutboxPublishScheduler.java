package com.twohands.admin_service.infrastructure.outbox;

import com.twohands.admin_service.application.outbox.PublishAdminEventsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxPublishScheduler {

	private static final Logger log = LoggerFactory.getLogger(OutboxPublishScheduler.class);

	private final PublishAdminEventsUseCase publishAdminEventsUseCase;
	private final boolean enabled;

	public OutboxPublishScheduler(
			PublishAdminEventsUseCase publishAdminEventsUseCase,
			@Value("${admin.outbox.publish.enabled:false}") boolean enabled
	) {
		this.publishAdminEventsUseCase = publishAdminEventsUseCase;
		this.enabled = enabled;
	}

	@Scheduled(cron = "${admin.outbox.publish.cron:0/10 * * * * *}")
	public void runPublishJob() {
		if (!enabled) {
			return;
		}
		int processed = publishAdminEventsUseCase.execute();
		if (processed > 0) {
			log.info("Outbox publish job completed. processedEvents={}", processed);
		}
	}
}
