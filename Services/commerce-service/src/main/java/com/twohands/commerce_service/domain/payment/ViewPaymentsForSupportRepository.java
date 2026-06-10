package com.twohands.commerce_service.domain.payment;

import com.twohands.commerce_service.domain.support.WebhookSupportPageRequest;

public interface ViewPaymentsForSupportRepository {

    PaymentSupportPagedResult search(PaymentSupportSearchCriteria criteria, WebhookSupportPageRequest pageRequest);
}
