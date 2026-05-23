package com.twohands.admin_service.application.support.viewpaymentdetail;

import java.util.UUID;

public record ViewPaymentSupportDetailQuery(UUID paymentId, String bearerToken) {
}
