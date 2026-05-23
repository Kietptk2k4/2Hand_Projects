package com.twohands.admin_service.application.enforcement.suspenduser;

import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.application.enforcement.UserEnforcementOutboxPayloadBuilder;
import com.twohands.admin_service.application.outbox.enqueue.InsertAdminOutboxEventCommand;
import com.twohands.admin_service.application.outbox.enqueue.InsertAdminOutboxEventUseCase;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.audit.AdminActionStatus;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.enforcement.UserEnforcement;
import com.twohands.admin_service.domain.enforcement.UserEnforcementActionType;
import com.twohands.admin_service.domain.enforcement.UserEnforcementLog;
import com.twohands.admin_service.domain.enforcement.UserEnforcementLogRepository;
import com.twohands.admin_service.domain.enforcement.UserEnforcementPolicy;
import com.twohands.admin_service.domain.enforcement.UserEnforcementRepository;
import com.twohands.admin_service.domain.enforcement.UserEnforcementStatus;
import com.twohands.admin_service.domain.integration.AuthUserEnforcementGateway;
import com.twohands.admin_service.domain.outbox.OutboxEvent;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class SuspendUserUseCase {

	private static final Logger log = LoggerFactory.getLogger(SuspendUserUseCase.class);
	private static final String ACTION_TYPE = "USER_SUSPEND";
	private static final String OUTBOX_EVENT_TYPE = "USER_SUSPENDED";
	private static final String SUCCESS_MESSAGE = "User suspended successfully";

	private final AdminAuthorizationService adminAuthorizationService;
	private final UserEnforcementRepository userEnforcementRepository;
	private final UserEnforcementLogRepository userEnforcementLogRepository;
	private final AuthUserEnforcementGateway authUserEnforcementGateway;
	private final InsertAdminOutboxEventUseCase insertAdminOutboxEventUseCase;
	private final UserEnforcementOutboxPayloadBuilder outboxPayloadBuilder;
	private final AdminActionAuditLogger adminActionAuditLogger;

	public SuspendUserUseCase(
			AdminAuthorizationService adminAuthorizationService,
			UserEnforcementRepository userEnforcementRepository,
			UserEnforcementLogRepository userEnforcementLogRepository,
			AuthUserEnforcementGateway authUserEnforcementGateway,
			InsertAdminOutboxEventUseCase insertAdminOutboxEventUseCase,
			UserEnforcementOutboxPayloadBuilder outboxPayloadBuilder,
			AdminActionAuditLogger adminActionAuditLogger
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.userEnforcementRepository = userEnforcementRepository;
		this.userEnforcementLogRepository = userEnforcementLogRepository;
		this.authUserEnforcementGateway = authUserEnforcementGateway;
		this.insertAdminOutboxEventUseCase = insertAdminOutboxEventUseCase;
		this.outboxPayloadBuilder = outboxPayloadBuilder;
		this.adminActionAuditLogger = adminActionAuditLogger;
	}

	@Transactional
	public SuspendUserResult execute(SuspendUserCommand command) {
		UUID adminId = adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.USER_SUSPEND);

		Instant now = Instant.now();
		UserEnforcementPolicy.validateSuspendRequest(command.reasonCode(), command.description(), command.expiresAt(), now);

		if (userEnforcementRepository.existsActiveByUserIdAndActionType(command.userId(), UserEnforcementActionType.SUSPEND)) {
			throw new AppException(ErrorCode.ENFORCEMENT_CONFLICT, ErrorCode.ENFORCEMENT_CONFLICT.defaultMessage());
		}

		UUID enforcementId = UUID.randomUUID();
		log.info("Suspending user. adminId={}, userId={}, enforcementId={}", adminId, command.userId(), enforcementId);

		try {
			if (authUserEnforcementGateway.isEnabled()) {
				authUserEnforcementGateway.suspendUser(new AuthUserEnforcementGateway.AuthSuspendUserRequest(
						command.userId(),
						enforcementId,
						command.reasonCode().trim(),
						command.description().trim(),
						command.expiresAt(),
						command.bearerToken()
				));
			}

			UserEnforcement enforcement = userEnforcementRepository.save(new UserEnforcement(
					enforcementId,
					command.userId(),
					UserEnforcementActionType.SUSPEND,
					command.reasonCode().trim(),
					command.description().trim(),
					command.expiresAt(),
					adminId,
					UserEnforcementStatus.ACTIVE,
					now,
					now
			));

			userEnforcementLogRepository.save(new UserEnforcementLog(
					UUID.randomUUID(),
					enforcement.id(),
					null,
					UserEnforcementStatus.ACTIVE,
					adminId,
					"Enforcement created",
					now
			));

			OutboxEvent outboxEvent = insertAdminOutboxEventUseCase.execute(new InsertAdminOutboxEventCommand(
					OUTBOX_EVENT_TYPE,
					command.userId(),
					outboxPayloadBuilder.buildUserSuspendedPayload(enforcement)
			));

			Map<String, Object> afterSummary = new LinkedHashMap<>();
			afterSummary.put("status", "ACTIVE");
			afterSummary.put("enforcement_id", enforcement.id().toString());
			if (enforcement.expiresAt() != null) {
				afterSummary.put("expires_at", enforcement.expiresAt().toString());
			}

			adminActionAuditLogger.logCritical(
					adminId,
					ACTION_TYPE,
					AdminActionTargetType.USER,
					command.userId().toString(),
					AdminActionStatus.SUCCESS,
					SUCCESS_MESSAGE,
					"User suspend enforcement created",
					Map.of("status", "ACTIVE", "action_type", "SUSPEND"),
					afterSummary,
					Map.of(
							"reason_code", enforcement.reasonCode(),
							"auth_integration", authUserEnforcementGateway.isEnabled()
					),
					Map.of("outbox_event_id", outboxEvent.id().toString())
			);

			return new SuspendUserResult(
					enforcement.id(),
					enforcement.userId(),
					enforcement.reasonCode(),
					enforcement.status(),
					enforcement.expiresAt(),
					enforcement.enforcedBy(),
					enforcement.createdAt(),
					outboxEvent.id()
			);
		} catch (AppException ex) {
			adminActionAuditLogger.logFailure(
					adminId,
					ACTION_TYPE,
					AdminActionTargetType.USER,
					command.userId().toString(),
					ex.getMessage(),
					Map.of(
							"reason_code", command.reasonCode(),
							"error_code", ex.getErrorCode().code()
					)
			);
			throw ex;
		}
	}

	public String successMessage() {
		return SUCCESS_MESSAGE;
	}
}
