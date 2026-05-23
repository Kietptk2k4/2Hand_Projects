package com.twohands.admin_service.domain.moderation;

import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;

import java.util.EnumSet;
import java.util.Set;

public final class SocialContentModerationPolicy {

	private static final Set<ContentModerationAction> POST_MODERATION_ACTIONS = EnumSet.of(
			ContentModerationAction.HIDE,
			ContentModerationAction.REMOVE
	);

	private SocialContentModerationPolicy() {
	}

	public static void validateModeratePostRequest(ContentModerationAction action, String reason, String note) {
		if (action == null || !POST_MODERATION_ACTIONS.contains(action)) {
			throw new AppException(
					ErrorCode.VALIDATION_ERROR,
					ErrorCode.VALIDATION_ERROR.defaultMessage(),
					"action",
					"Action must be HIDE or REMOVE"
			);
		}
		ProductModerationPolicy.validateHideRequest(reason, note);
	}
}
