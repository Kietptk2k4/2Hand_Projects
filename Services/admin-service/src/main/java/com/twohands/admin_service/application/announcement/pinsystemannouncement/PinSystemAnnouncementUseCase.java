package com.twohands.admin_service.application.announcement.pinsystemannouncement;

import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.announcement.SystemAnnouncement;
import com.twohands.admin_service.domain.announcement.SystemAnnouncementPolicy;
import com.twohands.admin_service.domain.announcement.SystemAnnouncementRepository;
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
public class PinSystemAnnouncementUseCase {

	private static final Logger log = LoggerFactory.getLogger(PinSystemAnnouncementUseCase.class);
	private static final String ACTION_TYPE = "SYSTEM_ANNOUNCEMENT_PIN";
	private static final String SUCCESS_MESSAGE = "System announcement pin updated successfully";
	private static final String IDEMPOTENT_MESSAGE = "System announcement is already in the requested pinned state";

	private final AdminAuthorizationService adminAuthorizationService;
	private final SystemAnnouncementRepository systemAnnouncementRepository;
	private final AdminActionAuditLogger adminActionAuditLogger;

	public PinSystemAnnouncementUseCase(
			AdminAuthorizationService adminAuthorizationService,
			SystemAnnouncementRepository systemAnnouncementRepository,
			AdminActionAuditLogger adminActionAuditLogger
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.systemAnnouncementRepository = systemAnnouncementRepository;
		this.adminActionAuditLogger = adminActionAuditLogger;
	}

	@Transactional
	public PinSystemAnnouncementResult execute(PinSystemAnnouncementCommand command) {
		UUID adminId = adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.SYSTEM_ANNOUNCEMENT_UPDATE);

		SystemAnnouncement existing = systemAnnouncementRepository.findById(command.announcementId())
				.orElseThrow(() -> new AppException(
						ErrorCode.RESOURCE_NOT_FOUND,
						ErrorCode.RESOURCE_NOT_FOUND.defaultMessage()
				));

		SystemAnnouncementPolicy.assertPinAllowed(existing.status());

		if (existing.pinned() == command.pinned()) {
			log.info(
					"System announcement pin idempotent. announcementId={}, isPinned={}",
					existing.id(),
					existing.pinned()
			);
			return toResult(existing, false);
		}

		log.info(
				"Updating system announcement pin. adminId={}, announcementId={}, isPinned={} -> {}",
				adminId,
				existing.id(),
				existing.pinned(),
				command.pinned()
		);

		try {
			SystemAnnouncement updated = systemAnnouncementRepository.save(new SystemAnnouncement(
					existing.id(),
					existing.title(),
					existing.content(),
					existing.severity(),
					command.pinned(),
					existing.dismissible(),
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
							"is_pinned", command.pinned(),
							"previous_is_pinned", existing.pinned()
					),
					Map.of(
							"announcement_id", updated.id().toString(),
							"status", updated.status().name(),
							"is_pinned", updated.pinned()
					)
			);

			return toResult(updated, true);
		} catch (AppException ex) {
			adminActionAuditLogger.logFailure(
					adminId,
					ACTION_TYPE,
					AdminActionTargetType.ANNOUNCEMENT,
					existing.id().toString(),
					ex.getMessage(),
					Map.of(
							"announcement_id", command.announcementId().toString(),
							"is_pinned", command.pinned(),
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

	private PinSystemAnnouncementResult toResult(SystemAnnouncement announcement, boolean stateChanged) {
		return new PinSystemAnnouncementResult(
				announcement.id(),
				announcement.title(),
				announcement.status(),
				announcement.pinned(),
				stateChanged
		);
	}
}
