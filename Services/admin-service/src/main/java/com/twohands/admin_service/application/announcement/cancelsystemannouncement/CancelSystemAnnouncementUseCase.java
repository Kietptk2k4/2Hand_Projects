package com.twohands.admin_service.application.announcement.cancelsystemannouncement;

import com.twohands.admin_service.application.announcement.SystemAnnouncementOutboxPayloadBuilder;
import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.application.outbox.enqueue.InsertAdminOutboxEventCommand;
import com.twohands.admin_service.application.outbox.enqueue.InsertAdminOutboxEventUseCase;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.announcement.SystemAnnouncement;
import com.twohands.admin_service.domain.announcement.SystemAnnouncementPolicy;
import com.twohands.admin_service.domain.announcement.SystemAnnouncementRepository;
import com.twohands.admin_service.domain.announcement.SystemAnnouncementStatus;
import com.twohands.admin_service.domain.audit.AdminActionStatus;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
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
public class CancelSystemAnnouncementUseCase {

	private static final Logger log = LoggerFactory.getLogger(CancelSystemAnnouncementUseCase.class);
	private static final String ACTION_TYPE = "SYSTEM_ANNOUNCEMENT_CANCEL";
	private static final String OUTBOX_EVENT_TYPE = "SYSTEM_ANNOUNCEMENT_CANCELLED";
	private static final String SUCCESS_MESSAGE = "System announcement cancelled successfully";
	private static final String IDEMPOTENT_MESSAGE = "System announcement is already cancelled";

	private final AdminAuthorizationService adminAuthorizationService;
	private final SystemAnnouncementRepository systemAnnouncementRepository;
	private final InsertAdminOutboxEventUseCase insertAdminOutboxEventUseCase;
	private final SystemAnnouncementOutboxPayloadBuilder outboxPayloadBuilder;
	private final AdminActionAuditLogger adminActionAuditLogger;

	public CancelSystemAnnouncementUseCase(
			AdminAuthorizationService adminAuthorizationService,
			SystemAnnouncementRepository systemAnnouncementRepository,
			InsertAdminOutboxEventUseCase insertAdminOutboxEventUseCase,
			SystemAnnouncementOutboxPayloadBuilder outboxPayloadBuilder,
			AdminActionAuditLogger adminActionAuditLogger
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.systemAnnouncementRepository = systemAnnouncementRepository;
		this.insertAdminOutboxEventUseCase = insertAdminOutboxEventUseCase;
		this.outboxPayloadBuilder = outboxPayloadBuilder;
		this.adminActionAuditLogger = adminActionAuditLogger;
	}

	@Transactional
	public CancelSystemAnnouncementResult execute(CancelSystemAnnouncementCommand command) {
		UUID adminId = adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.SYSTEM_ANNOUNCEMENT_CANCEL);

		SystemAnnouncement existing = systemAnnouncementRepository.findById(command.announcementId())
				.orElseThrow(() -> new AppException(
						ErrorCode.RESOURCE_NOT_FOUND,
						ErrorCode.RESOURCE_NOT_FOUND.defaultMessage()
				));

		if (existing.status() == SystemAnnouncementStatus.CANCELLED) {
			log.info("System announcement cancel idempotent. announcementId={}", existing.id());
			return toResult(existing, false, null);
		}

		if (!SystemAnnouncementPolicy.isCancellable(existing.status())) {
			throw new AppException(
					ErrorCode.SYSTEM_ANNOUNCEMENT_CONFLICT,
					"Announcement cannot be cancelled from status: " + existing.status().name()
			);
		}

		String previousStatus = existing.status().name();
		boolean wasSent = existing.status() == SystemAnnouncementStatus.SENT;
		Instant cancelledAt = Instant.now();

		log.info(
				"Cancelling system announcement. adminId={}, announcementId={}, previousStatus={}",
				adminId,
				existing.id(),
				previousStatus
		);

		try {
			SystemAnnouncement cancelled = systemAnnouncementRepository.save(new SystemAnnouncement(
					existing.id(),
					existing.title(),
					existing.content(),
					existing.severity(),
					existing.pinned(),
					existing.dismissible(),
					SystemAnnouncementStatus.CANCELLED,
					existing.createdBy(),
					existing.createdAt(),
					existing.sentAt()
			));

			UUID outboxEventId = null;
			if (wasSent) {
				OutboxEvent outboxEvent = insertAdminOutboxEventUseCase.execute(new InsertAdminOutboxEventCommand(
						OUTBOX_EVENT_TYPE,
						cancelled.id(),
						outboxPayloadBuilder.buildCancelledPayload(cancelled, previousStatus, cancelledAt)
				));
				outboxEventId = outboxEvent.id();
			}

			Map<String, Object> beforeSummary = new LinkedHashMap<>();
			beforeSummary.put("status", previousStatus);
			beforeSummary.put("sent_at", existing.sentAt());

			Map<String, Object> afterSummary = new LinkedHashMap<>();
			afterSummary.put("status", cancelled.status().name());
			afterSummary.put("cancelled_at", cancelledAt.toString());

			Map<String, Object> resultSummary = new LinkedHashMap<>();
			resultSummary.put("announcement_id", cancelled.id().toString());
			if (outboxEventId != null) {
				resultSummary.put("outbox_event_id", outboxEventId.toString());
			}

			adminActionAuditLogger.logCritical(
					adminId,
					ACTION_TYPE,
					AdminActionTargetType.ANNOUNCEMENT,
					cancelled.id().toString(),
					AdminActionStatus.SUCCESS,
					SUCCESS_MESSAGE,
					"System announcement cancelled",
					beforeSummary,
					afterSummary,
					Map.of("title", cancelled.title(), "previous_status", previousStatus),
					resultSummary
			);

			return toResult(cancelled, true, outboxEventId);
		} catch (AppException ex) {
			adminActionAuditLogger.logFailure(
					adminId,
					ACTION_TYPE,
					AdminActionTargetType.ANNOUNCEMENT,
					existing.id().toString(),
					ex.getMessage(),
					Map.of(
							"announcement_id", command.announcementId().toString(),
							"status", existing.status().name(),
							"error_code", ex.getErrorCode().code()
					)
			);
			throw ex;
		}
	}

	public String successMessage() {
		return SUCCESS_MESSAGE;
	}

	public String idempotentMessage() {
		return IDEMPOTENT_MESSAGE;
	}

	private CancelSystemAnnouncementResult toResult(
			SystemAnnouncement announcement,
			boolean stateChanged,
			UUID outboxEventId
	) {
		return new CancelSystemAnnouncementResult(
				announcement.id(),
				announcement.title(),
				announcement.status(),
				stateChanged,
				outboxEventId
		);
	}
}
