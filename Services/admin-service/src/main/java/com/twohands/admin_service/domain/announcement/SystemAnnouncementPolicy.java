package com.twohands.admin_service.domain.announcement;

import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;

public final class SystemAnnouncementPolicy {

	private static final int MAX_TITLE_LENGTH = 500;
	private static final int MAX_CONTENT_LENGTH = 50_000;

	private SystemAnnouncementPolicy() {
	}

	public static void validateCreateRequest(
			String title,
			String content,
			SystemAnnouncementSeverity severity
	) {
		validateTitle(title);
		validateContent(content);
		if (severity == null) {
			throw new AppException(ErrorCode.VALIDATION_ERROR, "severity is required");
		}
	}

	public static String normalizeTitle(String title) {
		return title == null ? "" : title.trim();
	}

	public static String normalizeContent(String content) {
		return content == null ? "" : content.trim();
	}

	public static SystemAnnouncementSeverity parseSeverity(String severity) {
		if (severity == null || severity.isBlank()) {
			throw new AppException(ErrorCode.VALIDATION_ERROR, "severity is required");
		}
		try {
			return SystemAnnouncementSeverity.valueOf(severity.trim().toUpperCase());
		} catch (IllegalArgumentException ex) {
			throw new AppException(
					ErrorCode.VALIDATION_ERROR,
					"severity must be one of: INFO, WARNING, CRITICAL"
			);
		}
	}

	public static boolean resolvePinned(Boolean pinned) {
		return pinned != null && pinned;
	}

	public static boolean resolveDismissible(Boolean dismissible) {
		return dismissible == null || dismissible;
	}

	public static void assertDraftForPublish(SystemAnnouncementStatus status) {
		if (status != SystemAnnouncementStatus.DRAFT) {
			throw new AppException(
					ErrorCode.SYSTEM_ANNOUNCEMENT_CONFLICT,
					ErrorCode.SYSTEM_ANNOUNCEMENT_CONFLICT.defaultMessage()
			);
		}
	}

	public static void assertPinAllowed(SystemAnnouncementStatus status) {
		if (status == SystemAnnouncementStatus.CANCELLED) {
			throw new AppException(
					ErrorCode.SYSTEM_ANNOUNCEMENT_CONFLICT,
					"Cancelled announcements cannot be pinned"
			);
		}
	}

	private static void validateTitle(String title) {
		String normalized = normalizeTitle(title);
		if (normalized.isEmpty()) {
			throw new AppException(ErrorCode.VALIDATION_ERROR, "title is required");
		}
		if (normalized.length() > MAX_TITLE_LENGTH) {
			throw new AppException(ErrorCode.VALIDATION_ERROR, "title must not exceed 500 characters");
		}
	}

	private static void validateContent(String content) {
		String normalized = normalizeContent(content);
		if (normalized.isEmpty()) {
			throw new AppException(ErrorCode.VALIDATION_ERROR, "content is required");
		}
		if (normalized.length() > MAX_CONTENT_LENGTH) {
			throw new AppException(ErrorCode.VALIDATION_ERROR, "content exceeds maximum length");
		}
	}
}
