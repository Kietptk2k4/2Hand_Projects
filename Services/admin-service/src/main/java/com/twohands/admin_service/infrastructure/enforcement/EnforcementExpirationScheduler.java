package com.twohands.admin_service.infrastructure.enforcement;

import com.twohands.admin_service.application.enforcement.expire.ExpireUserEnforcementsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EnforcementExpirationScheduler {

	private static final Logger log = LoggerFactory.getLogger(EnforcementExpirationScheduler.class);

	private final ExpireUserEnforcementsUseCase expireUserEnforcementsUseCase;
	private final boolean enabled;

	public EnforcementExpirationScheduler(
			ExpireUserEnforcementsUseCase expireUserEnforcementsUseCase,
			@Value("${admin.jobs.enforcement-expiration.enabled:false}") boolean enabled
	) {
		this.expireUserEnforcementsUseCase = expireUserEnforcementsUseCase;
		this.enabled = enabled;
	}

	@Scheduled(cron = "${admin.jobs.enforcement-expiration.cron:0 0 * * * *}")
	public void runExpirationJob() {
		if (!enabled) {
			return;
		}
		int expired = expireUserEnforcementsUseCase.execute().expiredCount();
		if (expired > 0) {
			log.info("Enforcement expiration scheduler completed. expiredCount={}", expired);
		}
	}
}
