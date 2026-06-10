package com.twohands.admin_service.unit.constant;

import com.twohands.admin_service.constant.AdminPermission;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminPermissionTest {

	@Test
	void shipmentSupportWritePermissionsAreKnown() {
		assertTrue(AdminPermission.isKnown(AdminPermission.SHIPMENT_SUPPORT_WRITE));
		assertTrue(AdminPermission.isKnown(AdminPermission.SHIPMENT_SUPPORT_FORCE_WRITE));
		assertTrue(AdminPermission.knownCodes().contains(AdminPermission.SHIPMENT_SUPPORT_WRITE));
		assertTrue(AdminPermission.knownCodes().contains(AdminPermission.SHIPMENT_SUPPORT_FORCE_WRITE));
	}
}