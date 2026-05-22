package com.twohands.commerce_service.unit.domain.product;

import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.product.UpdateProductInventoryStatusResolver;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateProductInventoryStatusResolverTest {

    @Test
    void shouldMoveActiveToOutOfStockWhenStockZero() {
        Optional<ProductStatus> target = UpdateProductInventoryStatusResolver.resolveTargetStatus(
                ProductStatus.ACTIVE,
                0,
                true
        );

        assertThat(target).contains(ProductStatus.OUT_OF_STOCK);
    }

    @Test
    void shouldRestoreOutOfStockToActiveWhenEligible() {
        Optional<ProductStatus> target = UpdateProductInventoryStatusResolver.resolveTargetStatus(
                ProductStatus.OUT_OF_STOCK,
                5,
                true
        );

        assertThat(target).contains(ProductStatus.ACTIVE);
    }

    @Test
    void shouldKeepOutOfStockWhenNotEligibleForRestore() {
        Optional<ProductStatus> target = UpdateProductInventoryStatusResolver.resolveTargetStatus(
                ProductStatus.OUT_OF_STOCK,
                5,
                false
        );

        assertThat(target).isEmpty();
    }

    @Test
    void shouldNotChangeDraftStatus() {
        Optional<ProductStatus> target = UpdateProductInventoryStatusResolver.resolveTargetStatus(
                ProductStatus.DRAFT,
                0,
                true
        );

        assertThat(target).isEmpty();
    }
}
