package com.twohands.admin_service.unit.moderation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.admin_service.application.moderation.ProductModerationOutboxPayloadBuilder;
import com.twohands.admin_service.domain.moderation.ContentModerationAction;
import com.twohands.admin_service.domain.moderation.ContentModerationLog;
import com.twohands.admin_service.domain.moderation.ContentModerationTargetType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProductModerationOutboxPayloadBuilderTest {

	private final ProductModerationOutboxPayloadBuilder builder =
			new ProductModerationOutboxPayloadBuilder(new ObjectMapper());

	@Test
	void buildProductRemovedPayload_includesSellerUserIdAndRemovalReason() throws Exception {
		UUID productId = UUID.randomUUID();
		UUID sellerUserId = UUID.randomUUID();
		ContentModerationLog moderationLog = moderationLog(ContentModerationAction.REMOVE, "Counterfeit listing");

		String json = builder.buildProductRemovedPayload(moderationLog, productId, sellerUserId);
		JsonNode payload = new ObjectMapper().readTree(json);

		assertThat(payload.get("product_id").asText()).isEqualTo(productId.toString());
		assertThat(payload.get("seller_user_id").asText()).isEqualTo(sellerUserId.toString());
		assertThat(payload.get("removal_reason").asText()).isEqualTo("Counterfeit listing");
		assertThat(payload.get("reason").asText()).isEqualTo("Counterfeit listing");
	}

	private ContentModerationLog moderationLog(ContentModerationAction action, String reason) {
		return new ContentModerationLog(
				UUID.randomUUID(),
				ContentModerationTargetType.PRODUCT,
				UUID.randomUUID().toString(),
				action,
				reason,
				UUID.randomUUID(),
				Instant.parse("2026-06-04T10:00:00Z"),
				null
		);
	}
}
