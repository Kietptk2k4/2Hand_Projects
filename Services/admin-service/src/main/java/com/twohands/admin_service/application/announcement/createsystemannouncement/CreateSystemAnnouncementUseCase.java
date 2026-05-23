package com.twohands.admin_service.application.announcement.createsystemannouncement;

import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.announcement.SystemAnnouncement;
import com.twohands.admin_service.domain.announcement.SystemAnnouncementPolicy;
import com.twohands.admin_service.domain.announcement.SystemAnnouncementRepository;
import com.twohands.admin_service.domain.announcement.SystemAnnouncementSeverity;
import com.twohands.admin_service.domain.announcement.SystemAnnouncementStatus;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.exception.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class CreateSystemAnnouncementUseCase {

	private static final Logger log = LoggerFactory.getLogger(CreateSystemAnnouncementUseCase.class);
	private static final String ACTION_TYPE = "SYSTEM_ANNOUNCEMENT_CREATE";
	private static final String SUCCESS_MESSAGE = "System announcement created successfully";

	private final AdminAuthorizationService adminAuthorizationService;
	private final SystemAnnouncementRepository systemAnnouncementRepository;
	private final AdminActionAuditLogger adminActionAuditLogger;

	public CreateSystemAnnouncementUseCase(
			AdminAuthorizationService adminAuthorizationService,
			SystemAnnouncementRepository systemAnnouncementRepository,
			AdminActionAuditLogger adminActionAuditLogger
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.systemAnnouncementRepository = systemAnnouncementRepository;
		this.adminActionAuditLogger = adminActionAuditLogger;
	}

	@Transactional
	public CreateSystemAnnouncementResult execute(CreateSystemAnnouncementCommand command) {
		UUID adminId = adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.SYSTEM_ANNOUNCEMENT_CREATE);

		String title = SystemAnnouncementPolicy.normalizeTitle(command.title());
		String content = SystemAnnouncementPolicy.normalizeContent(command.content());
		SystemAnnouncementSeverity severity = SystemAnnouncementPolicy.parseSeverity(command.severity());
		boolean pinned = SystemAnnouncementPolicy.resolvePinned(command.pinned());
		boolean dismissible = SystemAnnouncementPolicy.resolveDismissible(command.dismissible());

		SystemAnnouncementPolicy.validateCreateRequest(title, content, severity);

		Instant now = Instant.now();
		UUID announcementId = UUID.randomUUID();
		log.info("Creating system announcement draft. adminId={}, announcementId={}", adminId, announcementId);

		try {
			SystemAnnouncement announcement = systemAnnouncementRepository.save(new SystemAnnouncement(
					announcementId,
					title,
					content,
					severity,
					pinned,
					dismissible,
					SystemAnnouncementStatus.DRAFT,
					adminId,
					now,
					null
			));

			adminActionAuditLogger.logSuccess(
					adminId,
					ACTION_TYPE,
					AdminActionTargetType.ANNOUNCEMENT,
					announcement.id().toString(),
					SUCCESS_MESSAGE,
					Map.of(
							"title", announcement.title(),
							"severity", announcement.severity().name(),
							"is_pinned", announcement.pinned(),
							"dismissible", announcement.dismissible()
					),
					Map.of(
							"announcement_id", announcement.id().toString(),
							"status", announcement.status().name()
					)
			);

			return new CreateSystemAnnouncementResult(
					announcement.id(),
					announcement.title(),
					announcement.content(),
					announcement.severity(),
					announcement.pinned(),
					announcement.dismissible(),
					announcement.status(),
					announcement.createdBy(),
					announcement.createdAt()
			);
		} catch (AppException ex) {
			adminActionAuditLogger.logFailure(
					adminId,
					ACTION_TYPE,
					AdminActionTargetType.ANNOUNCEMENT,
					announcementId.toString(),
					ex.getMessage(),
					Map.of(
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
}
