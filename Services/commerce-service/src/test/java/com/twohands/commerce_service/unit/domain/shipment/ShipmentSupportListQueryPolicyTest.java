package com.twohands.commerce_service.unit.domain.shipment;

import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.domain.shipment.ShipmentSupportListQueryPolicy;
import com.twohands.commerce_service.domain.shipment.ShipmentSupportListSortField;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShipmentSupportListQueryPolicyTest {

	@Test
	void parseStatus_acceptsValidStatusAndBlank() {
		assertTrue(ShipmentSupportListQueryPolicy.parseStatus(null).isEmpty());
		assertTrue(ShipmentSupportListQueryPolicy.parseStatus("").isEmpty());
		assertEquals(Optional.of(ShipmentStatus.SHIPPED), ShipmentSupportListQueryPolicy.parseStatus("shipped"));
	}

	@Test
	void parseStatus_rejectsUnknownStatus() {
		AppException ex = assertThrows(AppException.class, () -> ShipmentSupportListQueryPolicy.parseStatus("UNKNOWN"));
		assertEquals(ErrorCode.VALIDATION_ERROR, ex.getErrorCode());
	}

	@Test
	void parseCarrier_acceptsValidCarrierAndBlank() {
		assertTrue(ShipmentSupportListQueryPolicy.parseCarrier(null).isEmpty());
		assertEquals(Optional.of(ShipmentCarrier.GHN), ShipmentSupportListQueryPolicy.parseCarrier("ghn"));
	}

	@Test
	void parseCarrier_rejectsUnknownCarrier() {
		AppException ex = assertThrows(AppException.class, () -> ShipmentSupportListQueryPolicy.parseCarrier("DHL"));
		assertEquals(ErrorCode.VALIDATION_ERROR, ex.getErrorCode());
	}

	@Test
	void parseSortField_acceptsSupportedValuesAndDefaults() {
		assertEquals(ShipmentSupportListSortField.UPDATED_AT, ShipmentSupportListQueryPolicy.parseSortField(null));
		assertEquals(ShipmentSupportListSortField.CREATED_AT, ShipmentSupportListQueryPolicy.parseSortField("created_at"));
		assertEquals(ShipmentSupportListSortField.SHIPPED_AT, ShipmentSupportListQueryPolicy.parseSortField("shipped_at"));
	}

	@Test
	void parseSortField_rejectsUnknownSort() {
		AppException ex = assertThrows(AppException.class, () -> ShipmentSupportListQueryPolicy.parseSortField("delivered_at"));
		assertEquals(ErrorCode.VALIDATION_ERROR, ex.getErrorCode());
	}
}
