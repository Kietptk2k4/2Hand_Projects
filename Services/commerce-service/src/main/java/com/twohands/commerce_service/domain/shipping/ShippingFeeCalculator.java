package com.twohands.commerce_service.domain.shipping;

import java.math.BigDecimal;

public interface ShippingFeeCalculator {

    BigDecimal calculate(ShippingFeeRequest request);
}
