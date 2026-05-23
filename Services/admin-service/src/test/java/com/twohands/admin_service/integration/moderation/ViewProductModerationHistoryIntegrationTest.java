package com.twohands.admin_service.integration.moderation;

import com.twohands.admin_service.application.moderation.removeproduct.RemoveProductCommand;
import com.twohands.admin_service.application.moderation.removeproduct.RemoveProductUseCase;
import com.twohands.admin_service.application.moderation.restoreproduct.RestoreProductCommand;
import com.twohands.admin_service.application.moderation.restoreproduct.RestoreProductUseCase;
import com.twohands.admin_service.application.moderation.viewproducthistory.ViewProductModerationHistoryQuery;
import com.twohands.admin_service.application.moderation.viewproducthistory.ViewProductModerationHistoryUseCase;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.integration.CommerceProductGateway;
import com.twohands.admin_service.domain.moderation.ContentModerationAction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ViewProductModerationHistoryIntegrationTest {

	@Autowired
	private RemoveProductUseCase removeProductUseCase;

	@Autowired
	private RestoreProductUseCase restoreProductUseCase;

	@Autowired
	private ViewProductModerationHistoryUseCase viewProductModerationHistoryUseCase;

	@MockBean
	private AdminAuthorizationService adminAuthorizationService;

	@MockBean
	private CommerceProductGateway commerceProductGateway;

	@Test
	void execute_returnsHistoryNewestFirstAfterRemoveAndRestore() {
		UUID adminId = UUID.randomUUID();
		UUID productId = UUID.randomUUID();

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);

		removeProductUseCase.execute(new RemoveProductCommand(productId, "Policy violation", "Remove note"));
		restoreProductUseCase.execute(new RestoreProductCommand(productId, "Appeal approved", "Restore note"));

		var history = viewProductModerationHistoryUseCase.execute(new ViewProductModerationHistoryQuery(
				productId,
				1,
				10
		));

		assertEquals(productId, history.productId());
		assertEquals(2, history.totalElements());
		assertEquals(2, history.history().size());
		assertEquals(ContentModerationAction.RESTORE, history.history().get(0).action());
		assertEquals("Appeal approved", history.history().get(0).reason());
		assertEquals(ContentModerationAction.REMOVE, history.history().get(1).action());
		assertEquals("Policy violation", history.history().get(1).reason());
		assertTrue(history.history().get(0).createdAt().isAfter(history.history().get(1).createdAt())
				|| history.history().get(0).createdAt().equals(history.history().get(1).createdAt()));
	}

	@Test
	void execute_returnsEmptyHistoryForUnknownProduct() {
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());

		var history = viewProductModerationHistoryUseCase.execute(new ViewProductModerationHistoryQuery(
				UUID.randomUUID(),
				1,
				10
		));

		assertTrue(history.history().isEmpty());
		assertEquals(0, history.totalElements());
	}
}
