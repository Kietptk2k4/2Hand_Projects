package com.twohands.admin_service.domain.support;

public record OrderSupportShippingAddress(
		String receiverName,
		String phone,
		String provinceCode,
		String districtCode,
		String wardCode,
		String addressDetail,
		String fullAddress
) {
}
