package com.twohands.admin_service.application.announcement.publishsystemannouncement;

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
public class PublishSystemAnnouncementUseCase {

	private static final Logger log = LoggerFactory.getLogger(PublishSystemAnnouncementUseCase.class);
	private static final String ACTION_TYPE = "SYSTEM_ANNOUNCEMENT_PUBLISH";
	private static final String OUTBOX_EVENT_TYPE = "SYSTEM_ANNOUNCEMENT_PUBLISHED";
	private static final String SUCCESS_MESSAGE = "System announcement published successfully";

	private final AdminAuthorizationService adminAuthorizationService;
	private final SystemAnnouncementRepository systemAnnouncementRepository;
	private final InsertAdminOutboxEventUseCase insertAdminOutboxEventUseCase;
	private final SystemAnnouncementOutboxPayloadBuilder outboxPayloadBuilder;
	private final AdminActionAuditLogger adminActionAuditLogger;

	public PublishSystemAnnouncementUseCase(
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
	public PublishSystemAnnouncementResult execute(PublishSystemAnnouncementCommand command) {
		UUID adminId = adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.SYSTEM_ANNOUNCEMENT_PUBLISH);

		SystemAnnouncement existing = systemAnnouncementRepository.findById(command.announcementId())
				.orElseThrow(() -> new AppException(
						ErrorCode.RESOURCE_NOT_FOUND,
						ErrorCode.RESOURCE_NOT_FOUND.defaultMessage()
				));

		SystemAnnouncementPolicy.assertDraftForPublish(existing.status());

		Instant sentAt = Instant.now();
		log.info(
				"Publishing system announcement. adminId={}, announcementId={}, severity={}",
				adminId,
				existing.id(),
				existing.severity()
		);

		try {
			SystemAnnouncement published = systemAnnouncementRepository.save(new SystemAnnouncement(
					existing.id(),
					existing.title(),
					existing.content(),
					existing.severity(),
					existing.pinned(),
					existing.dismissible(),
					SystemAnnouncementStatus.SENT,
					existing.createdBy(),
					existing.createdAt(),
					sentAt
			));

			OutboxEvent outboxEvent = insertAdminOutboxEventUseCase.execute(new InsertAdminOutboxEventCommand(
					OUTBOX_EVENT_TYPE,
					published.id(),
					outboxPayloadBuilder.buildPublishedPayload(published)
			));

			Map<String, Object> beforeSummary = new LinkedHashMap<>();
			beforeSummary.put("status", existing.status().name());
			beforeSummary.put("sent_at", existing.sentAt());

			Map<String, Object> afterSummary = new LinkedHashMap<>();
			afterSummary.put("status", published.status().name());
			afterSummary.put("sent_at", published.sentAt().toString());
			afterSummary.put("severity", published.severity().name());
			afterSummary.put("is_pinned", published.pinned());

			adminActionAuditLogger.logCritical(
					adminId,
					ACTION_TYPE,
					AdminActionTargetType.ANNOUNCEMENT,
					published.id().toString(),
					AdminActionStatus.SUCCESS,
					SUCCESS_MESSAGE,
					"System announcement published",
					beforeSummary,
					afterSummary,
					Map.of("title", published.title()),
					Map.of(
							"announcement_id", published.id().toString(),
							"outbox_event_id", outboxEvent.id().toString()
					)
			);

			return new PublishSystemAnnouncementResult(
					published.id(),
					published.title(),
					published.severity(),
					published.status(),
					published.pinned(),
					published.dismissible(),
					published.sentAt(),
					outboxEvent.id()
			);
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
}
