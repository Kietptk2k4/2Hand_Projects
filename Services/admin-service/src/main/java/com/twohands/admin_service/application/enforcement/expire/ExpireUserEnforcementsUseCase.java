package com.twohands.admin_service.application.enforcement.expire;

import com.twohands.admin_service.application.enforcement.UserEnforcementOutboxPayloadBuilder;
import com.twohands.admin_service.application.outbox.enqueue.InsertAdminOutboxEventCommand;
import com.twohands.admin_service.application.outbox.enqueue.InsertAdminOutboxEventUseCase;
import com.twohands.admin_service.domain.enforcement.UserEnforcement;
import com.twohands.admin_service.domain.enforcement.UserEnforcementLog;
import com.twohands.admin_service.domain.enforcement.UserEnforcementLogRepository;
import com.twohands.admin_service.domain.enforcement.UserEnforcementRepository;
import com.twohands.admin_service.domain.enforcement.UserEnforcementStatus;
import com.twohands.admin_service.domain.outbox.OutboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ExpireUserEnforcementsUseCase {

	private static final Logger log = LoggerFactory.getLogger(ExpireUserEnforcementsUseCase.class);
	private static final String OUTBOX_EVENT_TYPE = "USER_ENFORCEMENT_EXPIRED";
	private static final String EXPIRATION_LOG_NOTE = "Enforcement expired";

	private final UserEnforcementRepository userEnforcementRepository;
	private final UserEnforcementLogRepository userEnforcementLogRepository;
	private final InsertAdminOutboxEventUseCase insertAdminOutboxEventUseCase;
	private final UserEnforcementOutboxPayloadBuilder outboxPayloadBuilder;
	private final int batchSize;

	public ExpireUserEnforcementsUseCase(
			UserEnforcementRepository userEnforcementRepository,
			UserEnforcementLogRepository userEnforcementLogRepository,
			InsertAdminOutboxEventUseCase insertAdminOutboxEventUseCase,
			UserEnforcementOutboxPayloadBuilder outboxPayloadBuilder,
			@Value("${admin.jobs.enforcement-expiration.batch-size:100}") int batchSize
	) {
		this.userEnforcementRepository = userEnforcementRepository;
		this.userEnforcementLogRepository = userEnforcementLogRepository;
		this.insertAdminOutboxEventUseCase = insertAdminOutboxEventUseCase;
		this.outboxPayloadBuilder = outboxPayloadBuilder;
		this.batchSize = batchSize;
	}

	@Transactional
	public ExpireUserEnforcementsResult execute() {
		Instant now = Instant.now();
		List<UserEnforcement> candidates = userEnforcementRepository.claimActiveExpiredEnforcements(now, batchSize);

		if (candidates.isEmpty()) {
			return new ExpireUserEnforcementsResult(0);
		}

		int expiredCount = 0;
		for (UserEnforcement enforcement : candidates) {
			if (expireSingle(enforcement, now)) {
				expiredCount++;
			}
		}

		if (expiredCount > 0) {
			log.info("User enforcement expiration job completed. expiredCount={}", expiredCount);
		}

		return new ExpireUserEnforcementsResult(expiredCount);
	}

	private boolean expireSingle(UserEnforcement enforcement, Instant now) {
		if (enforcement.status() != UserEnforcementStatus.ACTIVE) {
			log.debug(
					"Skipping enforcement expiration (not ACTIVE). enforcementId={}, status={}",
					enforcement.id(),
					enforcement.status()
			);
			return false;
		}
		if (enforcement.expiresAt() == null || enforcement.expiresAt().isAfter(now)) {
			log.debug(
					"Skipping enforcement expiration (not yet expired). enforcementId={}, expiresAt={}",
					enforcement.id(),
					enforcement.expiresAt()
			);
			return false;
		}

		UserEnforcement expired = userEnforcementRepository.save(new UserEnforcement(
				enforcement.id(),
				enforcement.userId(),
				enforcement.actionType(),
				enforcement.reasonCode(),
				enforcement.description(),
				enforcement.expiresAt(),
				enforcement.enforcedBy(),
				UserEnforcementStatus.EXPIRED,
				enforcement.createdAt(),
				now
		));

		userEnforcementLogRepository.save(new UserEnforcementLog(
				UUID.randomUUID(),
				expired.id(),
				UserEnforcementStatus.ACTIVE,
				UserEnforcementStatus.EXPIRED,
				null,
				EXPIRATION_LOG_NOTE,
				now
		));

		OutboxEvent outboxEvent = insertAdminOutboxEventUseCase.execute(new InsertAdminOutboxEventCommand(
				OUTBOX_EVENT_TYPE,
				expired.userId(),
				outboxPayloadBuilder.buildUserEnforcementExpiredPayload(expired, now)
		));

		log.info(
				"User enforcement expired. enforcementId={}, userId={}, actionType={}, outboxEventId={}",
				expired.id(),
				expired.userId(),
				expired.actionType(),
				outboxEvent.id()
		);
		return true;
	}
}
