package com.twohands.admin_service.domain.integration;

import com.twohands.admin_service.domain.support.PaymentSupportDetail;

import java.util.UUID;

public interface CommercePaymentSupportGateway {

	boolean isEnabled();

	PaymentSupportDetail fetchPaymentSupportDetail(UUID paymentId, String bearerToken);
}
