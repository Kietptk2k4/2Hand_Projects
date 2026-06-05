package com.twohands.admin_service.unit.moderation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.admin_service.application.moderation.ReviewModerationOutboxPayloadBuilder;
import com.twohands.admin_service.domain.moderation.ContentModerationAction;
import com.twohands.admin_service.domain.moderation.ContentModerationLog;
import com.twohands.admin_service.domain.moderation.ContentModerationTargetType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewModerationOutboxPayloadBuilderTest {

	private final ReviewModerationOutboxPayloadBuilder builder =
			new ReviewModerationOutboxPayloadBuilder(new ObjectMapper());

	@Test
	void buildReviewHiddenPayload_includesReviewAuthorAndSellerUserIds() throws Exception {
		UUID reviewId = UUID.randomUUID();
		UUID reviewAuthorId = UUID.randomUUID();
		UUID sellerUserId = UUID.randomUUID();
		ContentModerationLog moderationLog = moderationLog("Spam content");

		String json = builder.buildReviewHiddenPayload(moderationLog, reviewId, reviewAuthorId, sellerUserId);
		JsonNode payload = new ObjectMapper().readTree(json);

		assertThat(payload.get("review_id").asText()).isEqualTo(reviewId.toString());
		assertThat(payload.get("review_author_id").asText()).isEqualTo(reviewAuthorId.toString());
		assertThat(payload.get("seller_user_id").asText()).isEqualTo(sellerUserId.toString());
		assertThat(payload.get("hidden_reason").asText()).isEqualTo("Spam content");
	}

	private ContentModerationLog moderationLog(String reason) {
		return new ContentModerationLog(
				UUID.randomUUID(),
				ContentModerationTargetType.REVIEW,
				UUID.randomUUID().toString(),
				ContentModerationAction.HIDE,
				reason,
				UUID.randomUUID(),
				Instant.parse("2026-06-04T10:00:00Z"),
				null
		);
	}
}
