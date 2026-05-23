package com.twohands.admin_service.application.moderation.restorepost;

import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.application.moderation.PostModerationOutboxPayloadBuilder;
import com.twohands.admin_service.application.outbox.enqueue.InsertAdminOutboxEventCommand;
import com.twohands.admin_service.application.outbox.enqueue.InsertAdminOutboxEventUseCase;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.audit.AdminActionStatus;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.integration.SocialPostGateway;
import com.twohands.admin_service.domain.moderation.ContentModerationAction;
import com.twohands.admin_service.domain.moderation.ContentModerationLog;
import com.twohands.admin_service.domain.moderation.ContentModerationLogRepository;
import com.twohands.admin_service.domain.moderation.ContentModerationTargetType;
import com.twohands.admin_service.domain.moderation.ProductModerationPolicy;
import com.twohands.admin_service.domain.moderation.SocialContentModerationPolicy;
import com.twohands.admin_service.domain.outbox.OutboxEvent;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class RestorePostUseCase {

	private static final Logger log = LoggerFactory.getLogger(RestorePostUseCase.class);
	private static final String ACTION_TYPE = "POST_RESTORE";
	private static final String OUTBOX_EVENT_TYPE = "POST_RESTORED";
	private static final String SUCCESS_MESSAGE = "Post restored successfully";

	private final AdminAuthorizationService adminAuthorizationService;
	private final ContentModerationLogRepository contentModerationLogRepository;
	private final SocialPostGateway socialPostGateway;
	private final InsertAdminOutboxEventUseCase insertAdminOutboxEventUseCase;
	private final PostModerationOutboxPayloadBuilder outboxPayloadBuilder;
	private final AdminActionAuditLogger adminActionAuditLogger;

	public RestorePostUseCase(
			AdminAuthorizationService adminAuthorizationService,
			ContentModerationLogRepository contentModerationLogRepository,
			SocialPostGateway socialPostGateway,
			InsertAdminOutboxEventUseCase insertAdminOutboxEventUseCase,
			PostModerationOutboxPayloadBuilder outboxPayloadBuilder,
			AdminActionAuditLogger adminActionAuditLogger
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.contentModerationLogRepository = contentModerationLogRepository;
		this.socialPostGateway = socialPostGateway;
		this.insertAdminOutboxEventUseCase = insertAdminOutboxEventUseCase;
		this.outboxPayloadBuilder = outboxPayloadBuilder;
		this.adminActionAuditLogger = adminActionAuditLogger;
	}

	@Transactional
	public RestorePostResult execute(RestorePostCommand command) {
		UUID adminId = adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requireAnyPermission(
				AdminPermission.POST_RESTORE,
				AdminPermission.POST_MODERATE
		);

		String postId = command.postId() == null ? null : command.postId().trim();
		if (postId == null || postId.isBlank()) {
			throw new AppException(
					ErrorCode.VALIDATION_ERROR,
					ErrorCode.VALIDATION_ERROR.defaultMessage(),
					"postId",
					"Post id is required"
			);
		}

		String reason = command.reason() == null ? null : command.reason().trim();
		String note = ProductModerationPolicy.normalizeOptionalNote(command.note());
		SocialContentModerationPolicy.validateRestorePostRequest(reason, note);

		if (socialPostGateway.isEnabled()) {
			socialPostGateway.ensurePostExists(postId);
		}

		Instant restoredAt = Instant.now();
		UUID moderationLogId = UUID.randomUUID();
		log.info(
				"Restoring post. adminId={}, postId={}, moderationLogId={}",
				adminId,
				postId,
				moderationLogId
		);

		try {
			ContentModerationLog moderationLog = contentModerationLogRepository.save(new ContentModerationLog(
					moderationLogId,
					ContentModerationTargetType.POST,
					postId,
					ContentModerationAction.RESTORE,
					reason,
					adminId,
					restoredAt,
					note
			));

			OutboxEvent outboxEvent = insertAdminOutboxEventUseCase.execute(new InsertAdminOutboxEventCommand(
					OUTBOX_EVENT_TYPE,
					resolveAggregateId(postId),
					outboxPayloadBuilder.buildPostRestoredPayload(moderationLog, postId)
			));

			Map<String, Object> afterSummary = new LinkedHashMap<>();
			afterSummary.put("action", ContentModerationAction.RESTORE.name());
			afterSummary.put("moderation_log_id", moderationLog.id().toString());
			afterSummary.put("restored_at", restoredAt.toString());

			Map<String, Object> requestSummary = new LinkedHashMap<>();
			requestSummary.put("reason", reason);
			if (note != null) {
				requestSummary.put("note", note);
			}

			adminActionAuditLogger.logCritical(
					adminId,
					ACTION_TYPE,
					AdminActionTargetType.POST,
					postId,
					AdminActionStatus.SUCCESS,
					SUCCESS_MESSAGE,
					"Post restore moderation recorded",
					Map.of(),
					afterSummary,
					requestSummary,
					Map.of("outbox_event_id", outboxEvent.id().toString())
			);

			return new RestorePostResult(
					postId,
					moderationLog.id(),
					moderationLog.reason(),
					moderationLog.note(),
					adminId,
					moderationLog.createdAt(),
					outboxEvent.id()
			);
		} catch (AppException ex) {
			adminActionAuditLogger.logFailure(
					adminId,
					ACTION_TYPE,
					AdminActionTargetType.POST,
					postId,
					ex.getMessage(),
					Map.of(
							"post_id", postId,
							"error_code", ex.getErrorCode().code()
					)
			);
			throw ex;
		}
	}

	public String successMessage() {
		return SUCCESS_MESSAGE;
	}

	private UUID resolveAggregateId(String postId) {
		return UUID.nameUUIDFromBytes(("admin:post:" + postId).getBytes(StandardCharsets.UTF_8));
	}
}
