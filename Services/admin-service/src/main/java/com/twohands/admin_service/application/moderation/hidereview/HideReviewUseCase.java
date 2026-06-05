package com.twohands.admin_service.application.moderation.hidereview;

import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.application.moderation.ReviewModerationOutboxPayloadBuilder;
import com.twohands.admin_service.application.outbox.enqueue.InsertAdminOutboxEventCommand;
import com.twohands.admin_service.application.outbox.enqueue.InsertAdminOutboxEventUseCase;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.audit.AdminActionStatus;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.integration.CommerceReviewGateway;
import com.twohands.admin_service.domain.moderation.ContentModerationAction;
import com.twohands.admin_service.domain.moderation.ContentModerationLog;
import com.twohands.admin_service.domain.moderation.ContentModerationLogRepository;
import com.twohands.admin_service.domain.moderation.ContentModerationTargetType;
import com.twohands.admin_service.domain.moderation.ProductModerationPolicy;
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
public class HideReviewUseCase {

	private static final Logger log = LoggerFactory.getLogger(HideReviewUseCase.class);
	private static final String ACTION_TYPE = "REVIEW_HIDE";
	private static final String OUTBOX_EVENT_TYPE = "REVIEW_HIDDEN";
	private static final String SUCCESS_MESSAGE = "Review hidden successfully";

	private final AdminAuthorizationService adminAuthorizationService;
	private final ContentModerationLogRepository contentModerationLogRepository;
	private final CommerceReviewGateway commerceReviewGateway;
	private final InsertAdminOutboxEventUseCase insertAdminOutboxEventUseCase;
	private final ReviewModerationOutboxPayloadBuilder outboxPayloadBuilder;
	private final AdminActionAuditLogger adminActionAuditLogger;

	public HideReviewUseCase(
			AdminAuthorizationService adminAuthorizationService,
			ContentModerationLogRepository contentModerationLogRepository,
			CommerceReviewGateway commerceReviewGateway,
			InsertAdminOutboxEventUseCase insertAdminOutboxEventUseCase,
			ReviewModerationOutboxPayloadBuilder outboxPayloadBuilder,
			AdminActionAuditLogger adminActionAuditLogger
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.contentModerationLogRepository = contentModerationLogRepository;
		this.commerceReviewGateway = commerceReviewGateway;
		this.insertAdminOutboxEventUseCase = insertAdminOutboxEventUseCase;
		this.outboxPayloadBuilder = outboxPayloadBuilder;
		this.adminActionAuditLogger = adminActionAuditLogger;
	}

	@Transactional
	public HideReviewResult execute(HideReviewCommand command) {
		UUID adminId = adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.REVIEW_HIDE);

		String reason = command.reason() == null ? null : command.reason().trim();
		String note = ProductModerationPolicy.normalizeOptionalNote(command.note());
		ProductModerationPolicy.validateHideRequest(reason, note);

		UUID reviewAuthorId = null;
		UUID sellerUserId = null;
		if (commerceReviewGateway.isEnabled()) {
			CommerceReviewGateway.CommerceReviewParties parties = commerceReviewGateway.findReviewParties(command.reviewId())
					.orElseThrow(() -> new AppException(
							ErrorCode.RESOURCE_NOT_FOUND,
							ErrorCode.RESOURCE_NOT_FOUND.defaultMessage()
					));
			reviewAuthorId = parties.reviewAuthorId();
			sellerUserId = parties.sellerUserId();
		}

		Instant hiddenAt = Instant.now();
		UUID moderationLogId = UUID.randomUUID();
		log.info(
				"Hiding review. adminId={}, reviewId={}, moderationLogId={}",
				adminId,
				command.reviewId(),
				moderationLogId
		);

		try {
			ContentModerationLog moderationLog = contentModerationLogRepository.save(new ContentModerationLog(
					moderationLogId,
					ContentModerationTargetType.REVIEW,
					command.reviewId().toString(),
					ContentModerationAction.HIDE,
					reason,
					adminId,
					hiddenAt,
					note
			));

			OutboxEvent outboxEvent = insertAdminOutboxEventUseCase.execute(new InsertAdminOutboxEventCommand(
					OUTBOX_EVENT_TYPE,
					command.reviewId(),
					outboxPayloadBuilder.buildReviewHiddenPayload(
							moderationLog,
							command.reviewId(),
							reviewAuthorId,
							sellerUserId
					)
			));

			Map<String, Object> afterSummary = new LinkedHashMap<>();
			afterSummary.put("action", ContentModerationAction.HIDE.name());
			afterSummary.put("moderation_log_id", moderationLog.id().toString());
			afterSummary.put("hidden_at", hiddenAt.toString());

			Map<String, Object> requestSummary = new LinkedHashMap<>();
			requestSummary.put("reason", reason);
			if (note != null) {
				requestSummary.put("note", note);
			}
			requestSummary.put("commerce_integration", commerceReviewGateway.isEnabled());

			adminActionAuditLogger.logCritical(
					adminId,
					ACTION_TYPE,
					AdminActionTargetType.REVIEW,
					command.reviewId().toString(),
					AdminActionStatus.SUCCESS,
					SUCCESS_MESSAGE,
					"Review hide moderation recorded",
					Map.of(),
					afterSummary,
					requestSummary,
					Map.of("outbox_event_id", outboxEvent.id().toString())
			);

			return new HideReviewResult(
					command.reviewId(),
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
					AdminActionTargetType.REVIEW,
					command.reviewId().toString(),
					ex.getMessage(),
					Map.of(
							"review_id", command.reviewId().toString(),
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
