package com.twohands.admin_service.infrastructure.outbox;

import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AdminOutboxTopicResolver {

	private static final Map<String, String> EVENT_TYPE_TO_TOPIC = Map.ofEntries(
			Map.entry("USER_SUSPENDED", "admin.user.suspended"),
			Map.entry("USER_BANNED", "admin.user.banned"),
			Map.entry("USER_RESTRICTED", "admin.user.restricted"),
			Map.entry("USER_ENFORCEMENT_REVOKED", "admin.user.enforcement_revoked"),
			Map.entry("USER_ENFORCEMENT_EXPIRED", "admin.user.enforcement_expired"),
			Map.entry("PRODUCT_REMOVED", "admin.product.removed"),
			Map.entry("PRODUCT_RESTORED", "admin.product.restored"),
			Map.entry("REVIEW_HIDDEN", "admin.review.hidden"),
			Map.entry("REVIEW_RESTORED", "admin.review.restored"),
			Map.entry("SHOP_SUSPENDED", "admin.shop.suspended"),
			Map.entry("SHOP_RESTORED", "admin.shop.restored"),
			Map.entry("SHOP_CLOSED", "admin.shop.closed"),
			Map.entry("POST_MODERATED", "admin.post.moderated"),
			Map.entry("COMMENT_MODERATED", "admin.comment.moderated"),
			Map.entry("SYSTEM_CONFIG_UPDATED", "admin.config.updated"),
			Map.entry("SYSTEM_ANNOUNCEMENT_PUBLISHED", "admin.announcement.published"),
			Map.entry("SYSTEM_ANNOUNCEMENT_CANCELLED", "admin.announcement.cancelled")
	);

	public String resolve(String eventType) {
		String topic = EVENT_TYPE_TO_TOPIC.get(eventType);
		if (topic == null) {
			throw new AppException(
					ErrorCode.INTERNAL_ERROR,
					"Unsupported outbox event type for publish: " + eventType
			);
		}
		return topic;
	}
}
