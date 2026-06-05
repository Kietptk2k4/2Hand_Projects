package com.twohands.admin_service.application.moderation.removeproduct;

import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.application.moderation.ProductModerationOutboxPayloadBuilder;
import com.twohands.admin_service.application.outbox.enqueue.InsertAdminOutboxEventCommand;
import com.twohands.admin_service.application.outbox.enqueue.InsertAdminOutboxEventUseCase;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.audit.AdminActionStatus;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.integration.CommerceProductGateway;
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
public class RemoveProductUseCase {

	private static final Logger log = LoggerFactory.getLogger(RemoveProductUseCase.class);
	private static final String ACTION_TYPE = "PRODUCT_REMOVE";
	private static final String OUTBOX_EVENT_TYPE = "PRODUCT_REMOVED";
	private static final String SUCCESS_MESSAGE = "Product removed successfully";

	private final AdminAuthorizationService adminAuthorizationService;
	private final ContentModerationLogRepository contentModerationLogRepository;
	private final CommerceProductGateway commerceProductGateway;
	private final InsertAdminOutboxEventUseCase insertAdminOutboxEventUseCase;
	private final ProductModerationOutboxPayloadBuilder outboxPayloadBuilder;
	private final AdminActionAuditLogger adminActionAuditLogger;

	public RemoveProductUseCase(
			AdminAuthorizationService adminAuthorizationService,
			ContentModerationLogRepository contentModerationLogRepository,
			CommerceProductGateway commerceProductGateway,
			InsertAdminOutboxEventUseCase insertAdminOutboxEventUseCase,
			ProductModerationOutboxPayloadBuilder outboxPayloadBuilder,
			AdminActionAuditLogger adminActionAuditLogger
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.contentModerationLogRepository = contentModerationLogRepository;
		this.commerceProductGateway = commerceProductGateway;
		this.insertAdminOutboxEventUseCase = insertAdminOutboxEventUseCase;
		this.outboxPayloadBuilder = outboxPayloadBuilder;
		this.adminActionAuditLogger = adminActionAuditLogger;
	}

	@Transactional
	public RemoveProductResult execute(RemoveProductCommand command) {
		UUID adminId = adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.PRODUCT_REMOVE);

		String reason = command.reason() == null ? null : command.reason().trim();
		String note = ProductModerationPolicy.normalizeOptionalNote(command.note());
		ProductModerationPolicy.validateRemoveRequest(reason, note);

		UUID sellerUserId = null;
		if (commerceProductGateway.isEnabled()) {
			commerceProductGateway.ensureProductExists(command.productId());
			sellerUserId = commerceProductGateway.findSellerUserId(command.productId())
					.orElseThrow(() -> new AppException(
							ErrorCode.RESOURCE_NOT_FOUND,
							ErrorCode.RESOURCE_NOT_FOUND.defaultMessage()
					));
			commerceProductGateway.removeProduct(command.productId(), adminId, reason);
		}

		Instant removedAt = Instant.now();
		UUID moderationLogId = UUID.randomUUID();
		log.info(
				"Removing product. adminId={}, productId={}, moderationLogId={}",
				adminId,
				command.productId(),
				moderationLogId
		);

		try {
			ContentModerationLog moderationLog = contentModerationLogRepository.save(new ContentModerationLog(
					moderationLogId,
					ContentModerationTargetType.PRODUCT,
					command.productId().toString(),
					ContentModerationAction.REMOVE,
					reason,
					adminId,
					removedAt,
					note
			));

			OutboxEvent outboxEvent = insertAdminOutboxEventUseCase.execute(new InsertAdminOutboxEventCommand(
					OUTBOX_EVENT_TYPE,
					command.productId(),
					outboxPayloadBuilder.buildProductRemovedPayload(moderationLog, command.productId(), sellerUserId)
			));

			Map<String, Object> afterSummary = new LinkedHashMap<>();
			afterSummary.put("action", ContentModerationAction.REMOVE.name());
			afterSummary.put("moderation_log_id", moderationLog.id().toString());
			afterSummary.put("removed_at", removedAt.toString());

			Map<String, Object> requestSummary = new LinkedHashMap<>();
			requestSummary.put("reason", reason);
			if (note != null) {
				requestSummary.put("note", note);
			}
			requestSummary.put("commerce_integration", commerceProductGateway.isEnabled());

			adminActionAuditLogger.logCritical(
					adminId,
					ACTION_TYPE,
					AdminActionTargetType.PRODUCT,
					command.productId().toString(),
					AdminActionStatus.SUCCESS,
					SUCCESS_MESSAGE,
					"Product remove moderation recorded",
					Map.of(),
					afterSummary,
					requestSummary,
					Map.of("outbox_event_id", outboxEvent.id().toString())
			);

			return new RemoveProductResult(
					command.productId(),
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
					AdminActionTargetType.PRODUCT,
					command.productId().toString(),
					ex.getMessage(),
					Map.of(
							"product_id", command.productId().toString(),
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
