package com.twohands.commerce_service.unit.application.support;

import com.twohands.commerce_service.application.support.viewwebhooklogs.ViewWebhookLogsForSupportQuery;
import com.twohands.commerce_service.application.support.viewwebhooklogs.ViewWebhookLogsForSupportResult;
import com.twohands.commerce_service.application.support.viewwebhooklogs.ViewWebhookLogsForSupportUseCase;
import com.twohands.commerce_service.domain.support.ViewWebhookLogsForSupportRepository;
import com.twohands.commerce_service.domain.support.WebhookLogSupportEntry;
import com.twohands.commerce_service.domain.support.WebhookLogSupportPagedResult;
import com.twohands.commerce_service.domain.support.WebhookLogSupportSearchCriteria;
import com.twohands.commerce_service.domain.support.WebhookSupportPageRequest;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ViewWebhookLogsForSupportUseCaseTest {

	private final ViewWebhookLogsForSupportRepository repository = mock(ViewWebhookLogsForSupportRepository.class);
	private final ViewWebhookLogsForSupportUseCase useCase = new ViewWebhookLogsForSupportUseCase(repository);

	@Test
	void execute_returnsPagedWebhookLogs() {
		WebhookLogSupportEntry entry = new WebhookLogSupportEntry(
				UUID.randomUUID(),
				"PAYOS",
				"PAYOS-123",
				"PAYMENT_SUCCESS",
				"PROCESSED",
				true,
				0,
				"PAYOS:PAYOS-123:PAYMENT_SUCCESS",
				Map.of("code", "00"),
				Instant.parse("2026-05-20T10:00:00Z")
		);
		when(repository.search(any(WebhookLogSupportSearchCriteria.class), eq(new WebhookSupportPageRequest(1, 20))))
				.thenReturn(new WebhookLogSupportPagedResult(List.of(entry), 1, 20, 1L, 1));

		ViewWebhookLogsForSupportResult result = useCase.execute(new ViewWebhookLogsForSupportQuery(
				"PAYOS",
				"PAYOS-123",
				"PROCESSED",
				null,
				null,
				1,
				20
		));

		assertEquals(1, result.logs().size());
		assertEquals("PAYOS", result.logs().getFirst().provider());
		verify(repository).search(any(WebhookLogSupportSearchCriteria.class), eq(new WebhookSupportPageRequest(1, 20)));
	}
}
