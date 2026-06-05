package com.twohands.admin_service.unit.moderation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.admin_service.application.moderation.ShopModerationOutboxPayloadBuilder;
import com.twohands.admin_service.domain.moderation.ContentModerationAction;
import com.twohands.admin_service.domain.moderation.ContentModerationLog;
import com.twohands.admin_service.domain.moderation.ContentModerationTargetType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ShopModerationOutboxPayloadBuilderTest {

	private final ShopModerationOutboxPayloadBuilder builder =
			new ShopModerationOutboxPayloadBuilder(new ObjectMapper());

	@Test
	void buildShopSuspendedPayload_includesShopOwnerIdAndSuspensionReason() throws Exception {
		UUID shopId = UUID.randomUUID();
		UUID shopOwnerId = UUID.randomUUID();
		ContentModerationLog moderationLog = moderationLog("Repeated policy violations");

		String json = builder.buildShopSuspendedPayload(moderationLog, shopId, shopOwnerId);
		JsonNode payload = new ObjectMapper().readTree(json);

		assertThat(payload.get("shop_id").asText()).isEqualTo(shopId.toString());
		assertThat(payload.get("shop_owner_id").asText()).isEqualTo(shopOwnerId.toString());
		assertThat(payload.get("suspension_reason").asText()).isEqualTo("Repeated policy violations");
	}

	private ContentModerationLog moderationLog(String reason) {
		return new ContentModerationLog(
				UUID.randomUUID(),
				ContentModerationTargetType.SHOP,
				UUID.randomUUID().toString(),
				ContentModerationAction.SUSPEND,
				reason,
				UUID.randomUUID(),
				Instant.parse("2026-06-04T10:00:00Z"),
				null
		);
	}
}
