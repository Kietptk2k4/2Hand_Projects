package com.twohands.admin_service.application.outbox.enqueue;

import com.twohands.admin_service.domain.outbox.OutboxEvent;
import com.twohands.admin_service.domain.outbox.OutboxEventRepository;
import com.twohands.admin_service.domain.outbox.OutboxStatus;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import com.twohands.admin_service.infrastructure.outbox.AdminOutboxTopicResolver;
import com.twohands.admin_service.infrastructure.outbox.OutboxPublishPayloadGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Inserts a PENDING outbox row in the same transaction as domain changes (Outbox Pattern write side).
 */
@Service
public class InsertAdminOutboxEventUseCase {

	private final OutboxEventRepository outboxEventRepository;
	private final OutboxPublishPayloadGuard payloadGuard;
	private final AdminOutboxTopicResolver topicResolver;

	public InsertAdminOutboxEventUseCase(
			OutboxEventRepository outboxEventRepository,
			OutboxPublishPayloadGuard payloadGuard,
			AdminOutboxTopicResolver topicResolver
	) {
		this.outboxEventRepository = outboxEventRepository;
		this.payloadGuard = payloadGuard;
		this.topicResolver = topicResolver;
	}

	@Transactional
	public OutboxEvent execute(InsertAdminOutboxEventCommand command) {
		validate(command);
		payloadGuard.assertSafeToPublish(command.payloadJson());
		topicResolver.resolve(command.eventType());

		OutboxEvent event = new OutboxEvent(
				UUID.randomUUID(),
				command.eventType().trim().toUpperCase(),
				command.aggregateId(),
				command.payloadJson(),
				OutboxStatus.PENDING,
				0,
				Instant.now(),
				null,
				null
		);
		return outboxEventRepository.save(event);
	}

	private void validate(InsertAdminOutboxEventCommand command) {
		if (command.eventType() == null || command.eventType().isBlank()) {
			throw new AppException(ErrorCode.VALIDATION_ERROR, "eventType is required", "eventType", "must not be blank");
		}
		if (command.aggregateId() == null) {
			throw new AppException(ErrorCode.VALIDATION_ERROR, "aggregateId is required", "aggregateId", "must not be null");
		}
		if (command.payloadJson() == null || command.payloadJson().isBlank()) {
			throw new AppException(ErrorCode.VALIDATION_ERROR, "payload is required", "payload", "must not be blank");
		}
	}
}
