package com.twohands.admin_service.application.moderation.reopenshop;

import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.application.moderation.ShopModerationOutboxPayloadBuilder;
import com.twohands.admin_service.application.outbox.enqueue.InsertAdminOutboxEventCommand;
import com.twohands.admin_service.application.outbox.enqueue.InsertAdminOutboxEventUseCase;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.audit.AdminActionStatus;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.moderation.ContentModerationAction;
import com.twohands.admin_service.domain.moderation.ContentModerationLog;
import com.twohands.admin_service.domain.moderation.ContentModerationLogRepository;
import com.twohands.admin_service.domain.moderation.ContentModerationTargetType;
import com.twohands.admin_service.domain.moderation.ProductModerationPolicy;
import com.twohands.admin_service.domain.outbox.OutboxEvent;
import com.twohands.admin_service.exception.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ReopenShopUseCase {

	private static final Logger log = LoggerFactory.getLogger(ReopenShopUseCase.class);
	private static final String ACTION_TYPE = "SHOP_RESTORE";
	private static final String OUTBOX_EVENT_TYPE = "SHOP_RESTORED";
	private static final String SUCCESS_MESSAGE = "Shop reopened successfully";

	private final AdminAuthorizationService adminAuthorizationService;
	private final ContentModerationLogRepository contentModerationLogRepository;
	private final InsertAdminOutboxEventUseCase insertAdminOutboxEventUseCase;
	private final ShopModerationOutboxPayloadBuilder outboxPayloadBuilder;
	private final AdminActionAuditLogger adminActionAuditLogger;

	public ReopenShopUseCase(
			AdminAuthorizationService adminAuthorizationService,
			ContentModerationLogRepository contentModerationLogRepository,
			InsertAdminOutboxEventUseCase insertAdminOutboxEventUseCase,
			ShopModerationOutboxPayloadBuilder outboxPayloadBuilder,
			AdminActionAuditLogger adminActionAuditLogger
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.contentModerationLogRepository = contentModerationLogRepository;
		this.insertAdminOutboxEventUseCase = insertAdminOutboxEventUseCase;
		this.outboxPayloadBuilder = outboxPayloadBuilder;
		this.adminActionAuditLogger = adminActionAuditLogger;
	}

	@Transactional
	public ReopenShopResult execute(ReopenShopCommand command) {
		UUID adminId = adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requireAnyPermission(
				AdminPermission.SHOP_RESTORE,
				AdminPermission.SHOP_SUSPEND
		);

		String reason = command.reason() == null ? null : command.reason().trim();
		String note = ProductModerationPolicy.normalizeOptionalNote(command.note());
		ProductModerationPolicy.validateReopenRequest(reason, note);

		Instant reopenedAt = Instant.now();
		UUID moderationLogId = UUID.randomUUID();
		log.info(
				"Reopening shop. adminId={}, shopId={}, moderationLogId={}",
				adminId,
				command.shopId(),
				moderationLogId
		);

		try {
			ContentModerationLog moderationLog = contentModerationLogRepository.save(new ContentModerationLog(
					moderationLogId,
					ContentModerationTargetType.SHOP,
					command.shopId().toString(),
					ContentModerationAction.RESTORE,
					reason,
					adminId,
					reopenedAt,
					note
			));

			OutboxEvent outboxEvent = insertAdminOutboxEventUseCase.execute(new InsertAdminOutboxEventCommand(
					OUTBOX_EVENT_TYPE,
					command.shopId(),
					outboxPayloadBuilder.buildShopRestoredPayload(moderationLog, command.shopId())
			));

			Map<String, Object> afterSummary = new LinkedHashMap<>();
			afterSummary.put("action", ContentModerationAction.RESTORE.name());
			afterSummary.put("moderation_log_id", moderationLog.id().toString());
			afterSummary.put("reopened_at", reopenedAt.toString());

			Map<String, Object> requestSummary = new LinkedHashMap<>();
			requestSummary.put("reason", reason);
			if (note != null) {
				requestSummary.put("note", note);
			}

			adminActionAuditLogger.logCritical(
					adminId,
					ACTION_TYPE,
					AdminActionTargetType.SHOP,
					command.shopId().toString(),
					AdminActionStatus.SUCCESS,
					SUCCESS_MESSAGE,
					"Shop reopen moderation recorded",
					Map.of(),
					afterSummary,
					requestSummary,
					Map.of("outbox_event_id", outboxEvent.id().toString())
			);

			return new ReopenShopResult(
					command.shopId(),
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
					AdminActionTargetType.SHOP,
					command.shopId().toString(),
					ex.getMessage(),
					Map.of(
							"shop_id", command.shopId().toString(),
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
