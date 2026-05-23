package com.twohands.admin_service.application.announcement.dismisssystemannouncement;

import com.twohands.admin_service.domain.announcement.SystemAnnouncement;
import com.twohands.admin_service.domain.announcement.SystemAnnouncementPolicy;
import com.twohands.admin_service.domain.announcement.SystemAnnouncementRepository;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * MVP: validates dismiss policy server-side; per-user dismissal is client-side
 * (Notification Service owns persisted dismiss — see FR_DismissAnnouncementNotification).
 */
@Service
public class DismissSystemAnnouncementUseCase {

	private static final Logger log = LoggerFactory.getLogger(DismissSystemAnnouncementUseCase.class);
	private static final String SUCCESS_MESSAGE = "System announcement can be dismissed";

	private final AdminAuthorizationService adminAuthorizationService;
	private final SystemAnnouncementRepository systemAnnouncementRepository;

	public DismissSystemAnnouncementUseCase(
			AdminAuthorizationService adminAuthorizationService,
			SystemAnnouncementRepository systemAnnouncementRepository
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.systemAnnouncementRepository = systemAnnouncementRepository;
	}

	@Transactional(readOnly = true)
	public DismissSystemAnnouncementResult execute(DismissSystemAnnouncementCommand command) {
		UUID actorId = adminAuthorizationService.requireCurrentAdminId();

		SystemAnnouncement announcement = systemAnnouncementRepository.findById(command.announcementId())
				.orElseThrow(() -> new AppException(
						ErrorCode.RESOURCE_NOT_FOUND,
						ErrorCode.RESOURCE_NOT_FOUND.defaultMessage()
				));

		SystemAnnouncementPolicy.assertActiveForDismiss(announcement.status());
		SystemAnnouncementPolicy.assertDismissible(announcement.dismissible());

		log.info(
				"System announcement dismiss validated. actorId={}, announcementId={}, status={}",
				actorId,
				announcement.id(),
				announcement.status()
		);

		return new DismissSystemAnnouncementResult(
				announcement.id(),
				announcement.title(),
				announcement.status(),
				announcement.dismissible(),
				true
		);
	}

	public String successMessage() {
		return SUCCESS_MESSAGE;
	}
}
