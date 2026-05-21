package com.twohands.commerce_service.domain.checkout;

import com.twohands.commerce_service.domain.catalog.ActiveProductPrice;
import com.twohands.commerce_service.domain.catalog.ProductPriceCalculator;

import java.math.BigDecimal;

public final class OrderLineTotalCalculator {

    private OrderLineTotalCalculator() {
    }

    public static BigDecimal unitPrice(ActiveProductPrice activePrice) {
        return ProductPriceCalculator.effectivePrice(activePrice.price(), activePrice.salePrice());
    }

    public static BigDecimal itemTotal(ActiveProductPrice activePrice, int quantity) {
        return unitPrice(activePrice).multiply(BigDecimal.valueOf(quantity));
    }
}
