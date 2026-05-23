package com.twohands.admin_service.application.enforcement.revokeuserenforcement;

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
public class RevokeUserEnforcementUseCase {

	private static final Logger log = LoggerFactory.getLogger(RevokeUserEnforcementUseCase.class);
	private static final String ACTION_TYPE = "USER_ENFORCEMENT_REVOKE";
	private static final String OUTBOX_EVENT_TYPE = "USER_ENFORCEMENT_REVOKED";
	private static final String SUCCESS_MESSAGE = "User enforcement revoked successfully";

	private final AdminAuthorizationService adminAuthorizationService;
	private final UserEnforcementRepository userEnforcementRepository;
	private final UserEnforcementLogRepository userEnforcementLogRepository;
	private final AuthUserEnforcementGateway authUserEnforcementGateway;
	private final InsertAdminOutboxEventUseCase insertAdminOutboxEventUseCase;
	private final UserEnforcementOutboxPayloadBuilder outboxPayloadBuilder;
	private final AdminActionAuditLogger adminActionAuditLogger;

	public RevokeUserEnforcementUseCase(
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
	public RevokeUserEnforcementResult execute(RevokeUserEnforcementCommand command) {
		UUID adminId = adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.USER_ENFORCEMENT_REVOKE);

		UserEnforcementPolicy.validateRevokeRequest(command.note(), command.reason());

		UserEnforcement enforcement = userEnforcementRepository.findById(command.enforcementId())
				.orElseThrow(() -> new AppException(
						ErrorCode.RESOURCE_NOT_FOUND,
						ErrorCode.RESOURCE_NOT_FOUND.defaultMessage()
				));

		UserEnforcementPolicy.ensureRevocable(enforcement.status());

		Instant now = Instant.now();
		log.info(
				"Revoking user enforcement. adminId={}, enforcementId={}, userId={}, actionType={}",
				adminId,
				enforcement.id(),
				enforcement.userId(),
				enforcement.actionType()
		);

		try {
			UserEnforcement revoked = userEnforcementRepository.save(new UserEnforcement(
					enforcement.id(),
					enforcement.userId(),
					enforcement.actionType(),
					enforcement.reasonCode(),
					enforcement.description(),
					enforcement.expiresAt(),
					enforcement.enforcedBy(),
					UserEnforcementStatus.REVOKED,
					enforcement.createdAt(),
					now
			));

			String logNote = buildLogNote(command.note(), command.reason());
			userEnforcementLogRepository.save(new UserEnforcementLog(
					UUID.randomUUID(),
					revoked.id(),
					UserEnforcementStatus.ACTIVE,
					UserEnforcementStatus.REVOKED,
					adminId,
					logNote,
					now
			));

			boolean reactivateAuth = shouldReactivateAuthUser(revoked);

			if (authUserEnforcementGateway.isEnabled()) {
				authUserEnforcementGateway.revokeEnforcement(new AuthUserEnforcementGateway.AuthRevokeEnforcementRequest(
						revoked.id(),
						revoked.userId(),
						revoked.actionType().name(),
						reactivateAuth,
						command.note(),
						command.reason(),
						command.bearerToken()
				));
			}

			OutboxEvent outboxEvent = insertAdminOutboxEventUseCase.execute(new InsertAdminOutboxEventCommand(
					OUTBOX_EVENT_TYPE,
					revoked.userId(),
					outboxPayloadBuilder.buildUserEnforcementRevokedPayload(
							revoked,
							adminId,
							command.note(),
							command.reason()
					)
			));

			Map<String, Object> afterSummary = new LinkedHashMap<>();
			afterSummary.put("status", "REVOKED");
			afterSummary.put("enforcement_id", revoked.id().toString());
			afterSummary.put("action_type", revoked.actionType().name());
			afterSummary.put("reactivate_auth", reactivateAuth);

			adminActionAuditLogger.logCritical(
					adminId,
					ACTION_TYPE,
					AdminActionTargetType.USER,
					revoked.userId().toString(),
					AdminActionStatus.SUCCESS,
					SUCCESS_MESSAGE,
					"User enforcement revoked",
					Map.of(
							"status", "ACTIVE",
							"action_type", enforcement.actionType().name()
					),
					afterSummary,
					Map.of(
							"enforcement_id", revoked.id().toString(),
							"auth_integration", authUserEnforcementGateway.isEnabled()
					),
					Map.of("outbox_event_id", outboxEvent.id().toString())
			);

			return new RevokeUserEnforcementResult(
					revoked.id(),
					revoked.userId(),
					revoked.actionType(),
					revoked.status(),
					adminId,
					revoked.updatedAt(),
					outboxEvent.id()
			);
		} catch (AppException ex) {
			adminActionAuditLogger.logFailure(
					adminId,
					ACTION_TYPE,
					AdminActionTargetType.USER,
					enforcement.userId().toString(),
					ex.getMessage(),
					Map.of(
							"enforcement_id", command.enforcementId().toString(),
							"error_code", ex.getErrorCode().code()
					)
			);
			throw ex;
		}
	}

	public String successMessage() {
		return SUCCESS_MESSAGE;
	}

	private boolean shouldReactivateAuthUser(UserEnforcement revoked) {
		if (revoked.actionType() != UserEnforcementActionType.SUSPEND
				&& revoked.actionType() != UserEnforcementActionType.BAN) {
			return false;
		}
		return !userEnforcementRepository.existsActiveByUserIdAndActionType(
				revoked.userId(),
				UserEnforcementActionType.SUSPEND
		) && !userEnforcementRepository.existsActiveByUserIdAndActionType(
				revoked.userId(),
				UserEnforcementActionType.BAN
		);
	}

	private String buildLogNote(String note, String reason) {
		if (note != null && !note.isBlank()) {
			return note.trim();
		}
		if (reason != null && !reason.isBlank()) {
			return reason.trim();
		}
		return "Enforcement revoked";
	}
}
