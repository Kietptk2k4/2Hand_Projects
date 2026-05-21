package com.twohands.commerce_service.domain.catalog;

import java.math.BigDecimal;

public final class ProductPriceCalculator {

    private ProductPriceCalculator() {
    }

    public static BigDecimal effectivePrice(BigDecimal price, BigDecimal salePrice) {
        if (salePrice != null && salePrice.compareTo(price) <= 0) {
            return salePrice;
        }
        return price;
    }
}
