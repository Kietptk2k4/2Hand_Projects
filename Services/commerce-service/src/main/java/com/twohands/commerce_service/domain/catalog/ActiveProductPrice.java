package com.twohands.commerce_service.domain.catalog;

import java.math.BigDecimal;

public record ActiveProductPrice(BigDecimal price, BigDecimal salePrice, BigDecimal effectivePrice) {
}
