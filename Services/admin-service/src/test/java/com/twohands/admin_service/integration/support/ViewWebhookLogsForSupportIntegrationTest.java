package com.twohands.admin_service.integration.support;

import com.twohands.admin_service.application.support.viewwebhooklogs.ViewWebhookLogsForSupportQuery;
import com.twohands.admin_service.application.support.viewwebhooklogs.ViewWebhookLogsForSupportUseCase;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.integration.CommerceWebhookSupportGateway;
import com.twohands.admin_service.domain.support.WebhookSupportLogEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ViewWebhookLogsForSupportIntegrationTest {

	@Autowired
	private ViewWebhookLogsForSupportUseCase viewWebhookLogsForSupportUseCase;

	@MockBean
	private AdminAuthorizationService adminAuthorizationService;

	@MockBean
	private CommerceWebhookSupportGateway commerceWebhookSupportGateway;

	@Test
	void execute_returnsWebhookLogsFromCommerceGateway() {
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(commerceWebhookSupportGateway.isEnabled()).thenReturn(true);
		when(commerceWebhookSupportGateway.searchWebhookLogs(
				eq("GHN"),
				isNull(),
				isNull(),
				isNull(),
				isNull(),
				eq(1),
				eq(10),
				eq("bearer")
		)).thenReturn(new PagedResult<>(
				List.of(new WebhookSupportLogEntry(
						UUID.randomUUID(),
						"GHN",
						"GHN-100",
						"delivered",
						"PROCESSED",
						null,
						0,
						"GHN:GHN-100:delivered",
						Map.of("status", "delivered"),
						Instant.parse("2026-05-20T11:00:00Z")
				)),
				1,
				10,
				1L,
				1
		));

		var result = viewWebhookLogsForSupportUseCase.execute(new ViewWebhookLogsForSupportQuery(
				"GHN",
				null,
				null,
				null,
				null,
				1,
				10,
				"bearer"
		));

		assertEquals(1, result.logs().size());
		assertEquals("GHN-100", result.logs().getFirst().referenceId());
	}
}
