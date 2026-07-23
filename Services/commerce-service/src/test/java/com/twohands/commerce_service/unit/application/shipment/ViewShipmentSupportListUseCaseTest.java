package com.twohands.commerce_service.unit.application.shipment;

import com.twohands.commerce_service.application.shipment.viewshipmentsupportlist.ViewShipmentSupportListQuery;
import com.twohands.commerce_service.application.shipment.viewshipmentsupportlist.ViewShipmentSupportListResult;
import com.twohands.commerce_service.application.shipment.viewshipmentsupportlist.ViewShipmentSupportListUseCase;
import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.domain.shipment.ShipmentSupportListEntry;
import com.twohands.commerce_service.domain.shipment.ShipmentSupportListPagedResult;
import com.twohands.commerce_service.domain.shipment.ShipmentSupportListSearchCriteria;
import com.twohands.commerce_service.domain.shipment.ShipmentSupportListSortField;
import com.twohands.commerce_service.domain.shipment.ViewShipmentSupportListRepository;
import com.twohands.commerce_service.domain.support.WebhookSupportPageRequest;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ViewShipmentSupportListUseCaseTest {

	private final ViewShipmentSupportListRepository repository = mock(ViewShipmentSupportListRepository.class);
	private final ViewShipmentSupportListUseCase useCase = new ViewShipmentSupportListUseCase(repository);

	@Test
	void execute_returnsPagedShipments() {
		ShipmentSupportListEntry entry = new ShipmentSupportListEntry(
				UUID.randomUUID(),
				UUID.randomUUID(),
				UUID.randomUUID(),
				"GHN",
				"SHIPPED",
				"TRACK-1",
				"GHN-1",
				Instant.parse("2026-05-20T08:00:00Z"),
				Instant.parse("2026-05-19T10:00:00Z"),
				Instant.parse("2026-05-20T08:00:00Z")
		);
		when(repository.search(any(ShipmentSupportListSearchCriteria.class), eq(new WebhookSupportPageRequest(1, 20))))
				.thenReturn(new ShipmentSupportListPagedResult(List.of(entry), 1, 20, 1L, 1));

		ViewShipmentSupportListResult result = useCase.execute(new ViewShipmentSupportListQuery(
				"SHIPPED",
				"GHN",
				"updated_at",
				null,
				null,
				null,
				null,
				1,
				20
		));

		assertEquals(1, result.shipments().size());
		assertEquals("GHN", result.shipments().getFirst().carrier());
		verify(repository).search(any(ShipmentSupportListSearchCriteria.class), eq(new WebhookSupportPageRequest(1, 20)));
	}
}
