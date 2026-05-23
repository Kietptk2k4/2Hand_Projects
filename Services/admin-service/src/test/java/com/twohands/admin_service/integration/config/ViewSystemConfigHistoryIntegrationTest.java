package com.twohands.admin_service.integration.config;

import com.twohands.admin_service.application.config.createsystemconfig.CreateSystemConfigCommand;
import com.twohands.admin_service.application.config.createsystemconfig.CreateSystemConfigUseCase;
import com.twohands.admin_service.application.config.updatesystemconfig.UpdateSystemConfigCommand;
import com.twohands.admin_service.application.config.updatesystemconfig.UpdateSystemConfigUseCase;
import com.twohands.admin_service.application.config.viewhistory.ViewSystemConfigHistoryQuery;
import com.twohands.admin_service.application.config.viewhistory.ViewSystemConfigHistoryUseCase;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ViewSystemConfigHistoryIntegrationTest {

	@Autowired
	private CreateSystemConfigUseCase createSystemConfigUseCase;

	@Autowired
	private UpdateSystemConfigUseCase updateSystemConfigUseCase;

	@Autowired
	private ViewSystemConfigHistoryUseCase viewSystemConfigHistoryUseCase;

	@MockBean
	private AdminAuthorizationService adminAuthorizationService;

	@Test
	void execute_returnsHistoryEntriesAfterCreateAndUpdate() {
		UUID adminId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);

		var created = createSystemConfigUseCase.execute(new CreateSystemConfigCommand(
				"PAYMENT_EXPIRE_MINUTES",
				"15",
				"INTEGER",
				"Payment timeout",
				true,
				"Initial"
		));

		updateSystemConfigUseCase.execute(new UpdateSystemConfigCommand(
				created.configId(),
				"30",
				null,
				"Extended SLA"
		));

		var history = viewSystemConfigHistoryUseCase.execute(new ViewSystemConfigHistoryQuery(
				created.configId(),
				1,
				10
		));

		assertEquals("PAYMENT_EXPIRE_MINUTES", history.configKey());
		assertEquals(2, history.totalElements());
		assertEquals(2, history.history().size());
		assertEquals("30", history.history().get(0).newValue());
		assertEquals("15", history.history().get(1).newValue());
		assertFalse(history.valuesMasked());
	}
}
