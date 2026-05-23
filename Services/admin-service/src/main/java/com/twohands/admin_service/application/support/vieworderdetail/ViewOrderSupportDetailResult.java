package com.twohands.admin_service.application.support.vieworderdetail;

import com.twohands.admin_service.domain.support.OrderSupportDetail;

public record ViewOrderSupportDetailResult(OrderSupportDetail detail, boolean contactFieldsMasked) {
}
