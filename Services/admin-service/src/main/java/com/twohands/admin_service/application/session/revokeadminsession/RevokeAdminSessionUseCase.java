package com.twohands.admin_service.application.session.revokeadminsession;

import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.audit.AdminActionStatus;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.auth.AdminSessionRevokeRequest;
import com.twohands.admin_service.domain.auth.AdminSessionRevokeResult;
import com.twohands.admin_service.domain.auth.AuthAdminSessionGateway;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class RevokeAdminSessionUseCase {

	private static final Logger log = LoggerFactory.getLogger(RevokeAdminSessionUseCase.class);
	private static final String ACTION_TYPE = "ADMIN_SESSION_REVOKE";
	private static final String SUCCESS_MESSAGE = "Admin session revoked successfully";

	private final AuthAdminSessionGateway authAdminSessionGateway;
	private final AdminAuthorizationService adminAuthorizationService;
	private final AdminActionAuditLogger adminActionAuditLogger;

	public RevokeAdminSessionUseCase(
			AuthAdminSessionGateway authAdminSessionGateway,
			AdminAuthorizationService adminAuthorizationService,
			AdminActionAuditLogger adminActionAuditLogger
	) {
		this.authAdminSessionGateway = authAdminSessionGateway;
		this.adminAuthorizationService = adminAuthorizationService;
		this.adminActionAuditLogger = adminActionAuditLogger;
	}

	@Transactional
	public RevokeAdminSessionResult execute(RevokeAdminSessionCommand command) {
		if (!authAdminSessionGateway.isEnabled()) {
			throw new AppException(
					ErrorCode.SERVICE_UNAVAILABLE,
					"Auth integration is required to revoke admin sessions. Enable admin.integrations.auth."
			);
		}

		UUID actorAdminId = adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.ADMIN_SESSION_REVOKE);

		log.info(
				"Revoking admin session. actorAdminId={}, sessionId={}, revokeAllSessions={}",
				actorAdminId,
				command.sessionId(),
				command.revokeAllSessions()
		);

		try {
			AdminSessionRevokeResult revoked = authAdminSessionGateway.revoke(new AdminSessionRevokeRequest(
					command.sessionId(),
					command.revokeAllSessions(),
					command.bearerToken()
			));

			RevokeAdminSessionResult result = new RevokeAdminSessionResult(
					revoked.targetAdminUserId(),
					revoked.sessionId(),
					revoked.revokedSessionCount(),
					revoked.revokeAllSessions()
			);

			adminActionAuditLogger.logCritical(
					actorAdminId,
					ACTION_TYPE,
					AdminActionTargetType.ADMIN_SESSION,
					command.sessionId().toString(),
					AdminActionStatus.SUCCESS,
					SUCCESS_MESSAGE,
					"Admin session revoked via Auth Service",
					Map.of(
							"session_id", command.sessionId().toString(),
							"revoke_all_sessions", command.revokeAllSessions()
					),
					Map.of(
							"target_admin_user_id", revoked.targetAdminUserId().toString(),
							"revoked_session_count", revoked.revokedSessionCount()
					),
					Map.of("actor_admin_id", actorAdminId.toString()),
					Map.of(
							"revoked_session_count", revoked.revokedSessionCount(),
							"revoke_all_sessions", revoked.revokeAllSessions()
					)
			);

			return result;
		} catch (AppException ex) {
			adminActionAuditLogger.logFailure(
					actorAdminId,
					ACTION_TYPE,
					AdminActionTargetType.ADMIN_SESSION,
					command.sessionId().toString(),
					ex.getMessage(),
					Map.of(
							"session_id", command.sessionId().toString(),
							"revoke_all_sessions", command.revokeAllSessions(),
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
