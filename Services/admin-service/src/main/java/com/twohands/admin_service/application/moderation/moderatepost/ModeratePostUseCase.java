package com.twohands.admin_service.application.moderation.moderatepost;

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
public class ModeratePostUseCase {

	private static final Logger log = LoggerFactory.getLogger(ModeratePostUseCase.class);
	private static final String ACTION_TYPE = "POST_MODERATE";
	private static final String OUTBOX_EVENT_TYPE = "POST_MODERATED";

	private final AdminAuthorizationService adminAuthorizationService;
	private final ContentModerationLogRepository contentModerationLogRepository;
	private final SocialPostGateway socialPostGateway;
	private final InsertAdminOutboxEventUseCase insertAdminOutboxEventUseCase;
	private final PostModerationOutboxPayloadBuilder outboxPayloadBuilder;
	private final AdminActionAuditLogger adminActionAuditLogger;

	public ModeratePostUseCase(
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
	public ModeratePostResult execute(ModeratePostCommand command) {
		UUID adminId = adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.POST_MODERATE);

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
		SocialContentModerationPolicy.validateModeratePostRequest(command.action(), reason, note);

		if (socialPostGateway.isEnabled()) {
			socialPostGateway.ensurePostExists(postId);
		}

		Instant moderatedAt = Instant.now();
		UUID moderationLogId = UUID.randomUUID();
		log.info(
				"Moderating post. adminId={}, postId={}, action={}, moderationLogId={}",
				adminId,
				postId,
				command.action(),
				moderationLogId
		);

		try {
			ContentModerationLog moderationLog = contentModerationLogRepository.save(new ContentModerationLog(
					moderationLogId,
					ContentModerationTargetType.POST,
					postId,
					command.action(),
					reason,
					adminId,
					moderatedAt,
					note
			));

			OutboxEvent outboxEvent = insertAdminOutboxEventUseCase.execute(new InsertAdminOutboxEventCommand(
					OUTBOX_EVENT_TYPE,
					resolveAggregateId(postId),
					outboxPayloadBuilder.buildPostModeratedPayload(moderationLog, postId)
			));

			Map<String, Object> afterSummary = new LinkedHashMap<>();
			afterSummary.put("action", command.action().name());
			afterSummary.put("moderation_log_id", moderationLog.id().toString());
			afterSummary.put("moderated_at", moderatedAt.toString());

			Map<String, Object> requestSummary = new LinkedHashMap<>();
			requestSummary.put("action", command.action().name());
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
					successMessage(command.action()),
					"Post moderation recorded",
					Map.of(),
					afterSummary,
					requestSummary,
					Map.of("outbox_event_id", outboxEvent.id().toString())
			);

			return new ModeratePostResult(
					postId,
					command.action(),
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
							"action", command.action().name(),
							"error_code", ex.getErrorCode().code()
					)
			);
			throw ex;
		}
	}

	public String successMessage(ContentModerationAction action) {
		return switch (action) {
			case HIDE -> "Post hidden successfully";
			case REMOVE -> "Post removed successfully";
			default -> "Post moderated successfully";
		};
	}

	private UUID resolveAggregateId(String postId) {
		return UUID.nameUUIDFromBytes(("admin:post:" + postId).getBytes(StandardCharsets.UTF_8));
	}
}
