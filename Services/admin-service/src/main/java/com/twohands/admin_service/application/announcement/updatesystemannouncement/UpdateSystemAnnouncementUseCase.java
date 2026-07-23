package com.twohands.admin_service.application.announcement.updatesystemannouncement;

import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.announcement.SystemAnnouncement;
import com.twohands.admin_service.domain.announcement.SystemAnnouncementPolicy;
import com.twohands.admin_service.domain.announcement.SystemAnnouncementRepository;
import com.twohands.admin_service.domain.announcement.SystemAnnouncementSeverity;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
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
public class UpdateSystemAnnouncementUseCase {

	private static final Logger log = LoggerFactory.getLogger(UpdateSystemAnnouncementUseCase.class);
	private static final String ACTION_TYPE = "SYSTEM_ANNOUNCEMENT_UPDATE";
	private static final String SUCCESS_MESSAGE = "System announcement updated successfully";

	private final AdminAuthorizationService adminAuthorizationService;
	private final SystemAnnouncementRepository systemAnnouncementRepository;
	private final AdminActionAuditLogger adminActionAuditLogger;

	public UpdateSystemAnnouncementUseCase(
			AdminAuthorizationService adminAuthorizationService,
			SystemAnnouncementRepository systemAnnouncementRepository,
			AdminActionAuditLogger adminActionAuditLogger
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.systemAnnouncementRepository = systemAnnouncementRepository;
		this.adminActionAuditLogger = adminActionAuditLogger;
	}

	@Transactional
	public UpdateSystemAnnouncementResult execute(UpdateSystemAnnouncementCommand command) {
		UUID adminId = adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.SYSTEM_ANNOUNCEMENT_UPDATE);

		SystemAnnouncement existing = systemAnnouncementRepository.findById(command.announcementId())
				.orElseThrow(() -> new AppException(
						ErrorCode.RESOURCE_NOT_FOUND,
						ErrorCode.RESOURCE_NOT_FOUND.defaultMessage()
				));

		SystemAnnouncementPolicy.assertDraftForUpdate(existing.status());

		String title = SystemAnnouncementPolicy.normalizeTitle(command.title());
		String content = SystemAnnouncementPolicy.normalizeContent(command.content());
		SystemAnnouncementSeverity severity = SystemAnnouncementPolicy.parseSeverity(command.severity());
		boolean pinned = SystemAnnouncementPolicy.resolvePinned(command.pinned());
		boolean dismissible = SystemAnnouncementPolicy.resolveDismissible(command.dismissible());

		SystemAnnouncementPolicy.validateCreateRequest(title, content, severity);

		log.info(
				"Updating system announcement draft. adminId={}, announcementId={}",
				adminId,
				existing.id()
		);

		try {
			SystemAnnouncement updated = systemAnnouncementRepository.save(new SystemAnnouncement(
					existing.id(),
					title,
					content,
					severity,
					pinned,
					dismissible,
					existing.status(),
					existing.createdBy(),
					existing.createdAt(),
					existing.sentAt()
			));

			adminActionAuditLogger.logSuccess(
					adminId,
					ACTION_TYPE,
					AdminActionTargetType.ANNOUNCEMENT,
					updated.id().toString(),
					SUCCESS_MESSAGE,
					Map.of(
							"title", updated.title(),
							"severity", updated.severity().name(),
							"is_pinned", updated.pinned(),
							"dismissible", updated.dismissible()
					),
					Map.of(
							"announcement_id", updated.id().toString(),
							"status", updated.status().name()
					)
			);

			return toResult(updated);
		} catch (AppException ex) {
			adminActionAuditLogger.logFailure(
					adminId,
					ACTION_TYPE,
					AdminActionTargetType.ANNOUNCEMENT,
					existing.id().toString(),
					ex.getMessage(),
					Map.of(
							"announcement_id", command.announcementId().toString(),
							"title", title,
							"severity", severity.name(),
							"error_code", ex.getErrorCode().code()
					)
			);
			throw ex;
		}
	}

	public String successMessage() {
		return SUCCESS_MESSAGE;
	}

	private UpdateSystemAnnouncementResult toResult(SystemAnnouncement announcement) {
		return new UpdateSystemAnnouncementResult(
				announcement.id(),
				announcement.title(),
				announcement.content(),
				announcement.severity(),
				announcement.status(),
				announcement.pinned(),
				announcement.dismissible(),
				announcement.createdBy(),
				announcement.createdAt(),
				announcement.sentAt()
		);
	}
}
