package com.twohands.admin_service.domain.moderation;

import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;

public final class ProductModerationPolicy {

	private static final int MAX_REASON_LENGTH = 4000;
	private static final int MAX_NOTE_LENGTH = 4000;

	private ProductModerationPolicy() {
	}

	public static void validateRemoveRequest(String reason, String note) {
		validateModerationRequest(reason, note);
	}

	public static void validateRestoreRequest(String reason, String note) {
		validateModerationRequest(reason, note);
	}

	public static String normalizeOptionalNote(String note) {
		if (note == null) {
			return null;
		}
		String trimmed = note.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private static void validateModerationRequest(String reason, String note) {
		if (reason == null || reason.isBlank()) {
			throw validationError("reason", "Reason is required");
		}
		if (reason.length() > MAX_REASON_LENGTH) {
			throw validationError("reason", "Reason must be at most 4000 characters");
		}
		if (note != null && note.length() > MAX_NOTE_LENGTH) {
			throw validationError("note", "Note must be at most 4000 characters");
		}
	}

	private static AppException validationError(String field, String message) {
		return new AppException(ErrorCode.VALIDATION_ERROR, ErrorCode.VALIDATION_ERROR.defaultMessage(), field, message);
	}
}
