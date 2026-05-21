package com.twohands.commerce_service.infrastructure.persistence.jpa.mapper;

import com.twohands.commerce_service.domain.cart.CartItemStatus;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.shop.ShopStatus;
import com.twohands.commerce_service.infrastructure.persistence.jpa.enums.CartItemStatusType;
import com.twohands.commerce_service.infrastructure.persistence.jpa.enums.ProductStatusType;
import com.twohands.commerce_service.infrastructure.persistence.jpa.enums.ShopStatusType;

public final class PersistenceEnumMapper {

    private PersistenceEnumMapper() {
    }

    public static CartItemStatus toDomain(CartItemStatusType status) {
        return CartItemStatus.valueOf(status.name());
    }

    public static CartItemStatusType toEntity(CartItemStatus status) {
        return CartItemStatusType.valueOf(status.name());
    }

    public static ProductStatus toDomain(ProductStatusType status) {
        return ProductStatus.valueOf(status.name());
    }

    public static ShopStatus toDomain(ShopStatusType status) {
        return ShopStatus.valueOf(status.name());
    }
}
