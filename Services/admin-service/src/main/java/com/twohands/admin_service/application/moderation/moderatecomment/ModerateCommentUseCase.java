package com.twohands.admin_service.application.moderation.moderatecomment;

import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.application.moderation.CommentModerationOutboxPayloadBuilder;
import com.twohands.admin_service.application.outbox.enqueue.InsertAdminOutboxEventCommand;
import com.twohands.admin_service.application.outbox.enqueue.InsertAdminOutboxEventUseCase;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.audit.AdminActionStatus;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.integration.SocialCommentGateway;
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
public class ModerateCommentUseCase {

	private static final Logger log = LoggerFactory.getLogger(ModerateCommentUseCase.class);
	private static final String ACTION_TYPE = "COMMENT_MODERATE";
	private static final String OUTBOX_EVENT_TYPE = "COMMENT_MODERATED";

	private final AdminAuthorizationService adminAuthorizationService;
	private final ContentModerationLogRepository contentModerationLogRepository;
	private final SocialCommentGateway socialCommentGateway;
	private final InsertAdminOutboxEventUseCase insertAdminOutboxEventUseCase;
	private final CommentModerationOutboxPayloadBuilder outboxPayloadBuilder;
	private final AdminActionAuditLogger adminActionAuditLogger;

	public ModerateCommentUseCase(
			AdminAuthorizationService adminAuthorizationService,
			ContentModerationLogRepository contentModerationLogRepository,
			SocialCommentGateway socialCommentGateway,
			InsertAdminOutboxEventUseCase insertAdminOutboxEventUseCase,
			CommentModerationOutboxPayloadBuilder outboxPayloadBuilder,
			AdminActionAuditLogger adminActionAuditLogger
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.contentModerationLogRepository = contentModerationLogRepository;
		this.socialCommentGateway = socialCommentGateway;
		this.insertAdminOutboxEventUseCase = insertAdminOutboxEventUseCase;
		this.outboxPayloadBuilder = outboxPayloadBuilder;
		this.adminActionAuditLogger = adminActionAuditLogger;
	}

	@Transactional
	public ModerateCommentResult execute(ModerateCommentCommand command) {
		UUID adminId = adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.COMMENT_MODERATE);

		String commentId = command.commentId() == null ? null : command.commentId().trim();
		if (commentId == null || commentId.isBlank()) {
			throw new AppException(
					ErrorCode.VALIDATION_ERROR,
					ErrorCode.VALIDATION_ERROR.defaultMessage(),
					"commentId",
					"Comment id is required"
			);
		}

		String reason = command.reason() == null ? null : command.reason().trim();
		String note = ProductModerationPolicy.normalizeOptionalNote(command.note());
		SocialContentModerationPolicy.validateModerateCommentRequest(command.action(), reason, note);

		UUID authorUserId = null;
		String postId = null;
		if (socialCommentGateway.isEnabled()) {
			socialCommentGateway.ensureCommentExists(commentId);
			authorUserId = socialCommentGateway.findAuthorUserId(commentId)
					.orElseThrow(() -> new AppException(
							ErrorCode.RESOURCE_NOT_FOUND,
							ErrorCode.RESOURCE_NOT_FOUND.defaultMessage()
					));
			postId = socialCommentGateway.findPostId(commentId).orElse(null);
		}

		Instant moderatedAt = Instant.now();
		UUID moderationLogId = UUID.randomUUID();
		log.info(
				"Moderating comment. adminId={}, commentId={}, action={}, moderationLogId={}",
				adminId,
				commentId,
				command.action(),
				moderationLogId
		);

		try {
			ContentModerationLog moderationLog = contentModerationLogRepository.save(new ContentModerationLog(
					moderationLogId,
					ContentModerationTargetType.COMMENT,
					commentId,
					command.action(),
					reason,
					adminId,
					moderatedAt,
					note
			));

			OutboxEvent outboxEvent = insertAdminOutboxEventUseCase.execute(new InsertAdminOutboxEventCommand(
					OUTBOX_EVENT_TYPE,
					resolveAggregateId(commentId),
					outboxPayloadBuilder.buildCommentModeratedPayload(moderationLog, commentId, authorUserId, postId)
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
					AdminActionTargetType.COMMENT,
					commentId,
					AdminActionStatus.SUCCESS,
					successMessage(command.action()),
					"Comment moderation recorded",
					Map.of(),
					afterSummary,
					requestSummary,
					Map.of("outbox_event_id", outboxEvent.id().toString())
			);

			return new ModerateCommentResult(
					commentId,
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
					AdminActionTargetType.COMMENT,
					commentId,
					ex.getMessage(),
					Map.of(
							"comment_id", commentId,
							"action", command.action().name(),
							"error_code", ex.getErrorCode().code()
					)
			);
			throw ex;
		}
	}

	public String successMessage(ContentModerationAction action) {
		return switch (action) {
			case HIDE -> "Comment hidden successfully";
			case REMOVE -> "Comment removed successfully";
			default -> "Comment moderated successfully";
		};
	}

	private UUID resolveAggregateId(String commentId) {
		return UUID.nameUUIDFromBytes(("admin:comment:" + commentId).getBytes(StandardCharsets.UTF_8));
	}
}
