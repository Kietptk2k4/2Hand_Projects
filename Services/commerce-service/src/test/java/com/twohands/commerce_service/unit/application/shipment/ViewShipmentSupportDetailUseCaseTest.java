package com.twohands.commerce_service.unit.application.shipment;

import com.twohands.commerce_service.application.shipment.viewshipmentsupport.ViewShipmentSupportDetailCommand;
import com.twohands.commerce_service.application.shipment.viewshipmentsupport.ViewShipmentSupportDetailUseCase;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.shipment.GhnWebhookSummary;
import com.twohands.commerce_service.domain.shipment.ShipmentAddressSnapshot;
import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.domain.shipment.ShipmentSupportDetailSnapshot;
import com.twohands.commerce_service.domain.shipment.SellerShipmentRecord;
import com.twohands.commerce_service.domain.shipment.ViewShipmentSupportDetailRepository;
import com.twohands.commerce_service.domain.shipment.ViewShipmentSupportDetailResult;
import com.twohands.commerce_service.domain.shipping.ShipmentType;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ViewShipmentSupportDetailUseCaseTest {

	private final ViewShipmentSupportDetailRepository repository = mock(ViewShipmentSupportDetailRepository.class);
	private final ViewShipmentSupportDetailUseCase useCase = new ViewShipmentSupportDetailUseCase(repository);

	@Test
	void execute_returnsSupportDetailWithCarrierStatus() {
		UUID shipmentId = UUID.randomUUID();
		SellerShipmentRecord shipment = new SellerShipmentRecord(
				shipmentId,
				UUID.randomUUID(),
				UUID.randomUUID(),
				ShipmentCarrier.GHN,
				ShipmentType.STANDARD,
				ShipmentStatus.SHIPPED,
				"GHN-001",
				"TRACK-1",
				BigDecimal.TEN,
				BigDecimal.ZERO,
				500,
				null,
				Instant.parse("2026-05-20T08:00:00Z"),
				null,
				Instant.parse("2026-05-19T00:00:00Z"),
				Instant.parse("2026-05-20T00:00:00Z")
		);
		ShipmentSupportDetailSnapshot snapshot = new ShipmentSupportDetailSnapshot(
				shipment,
				UUID.randomUUID(),
				OrderStatus.PROCESSING,
				new ShipmentAddressSnapshot("A", "090", "79", "760", "1", "detail", "full"),
				List.of(),
				List.of(),
				List.of(new GhnWebhookSummary("transporting", true, Instant.parse("2026-05-20T09:00:00Z")))
		);
		when(repository.findByShipmentId(shipmentId)).thenReturn(Optional.of(snapshot));

		ViewShipmentSupportDetailResult result = useCase.execute(new ViewShipmentSupportDetailCommand(shipmentId));

		assertEquals(shipmentId, result.shipment().shipmentId());
		assertEquals("transporting", result.carrierStatus());
		assertEquals(ShipmentStatus.SHIPPED, result.shipment().status());
	}

	@Test
	void execute_throwsWhenShipmentNotFound() {
		UUID shipmentId = UUID.randomUUID();
		when(repository.findByShipmentId(shipmentId)).thenReturn(Optional.empty());

		AppException ex = assertThrows(AppException.class, () -> useCase.execute(new ViewShipmentSupportDetailCommand(shipmentId)));

		assertEquals(ErrorCode.SHIPMENT_NOT_FOUND, ex.getErrorCode());
	}
}
