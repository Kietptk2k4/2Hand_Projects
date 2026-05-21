package com.twohands.commerce_service.unit.domain.product;

import com.twohands.commerce_service.domain.product.Product;
import com.twohands.commerce_service.domain.product.ProductStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProductTest {

    private final UUID sellerId = UUID.randomUUID();

    @Test
    void shouldAllowArchiveFromDraftActivePausedAndOutOfStock() {
        assertThat(productWithStatus(ProductStatus.DRAFT).canArchive()).isTrue();
        assertThat(productWithStatus(ProductStatus.ACTIVE).canArchive()).isTrue();
        assertThat(productWithStatus(ProductStatus.PAUSED).canArchive()).isTrue();
        assertThat(productWithStatus(ProductStatus.OUT_OF_STOCK).canArchive()).isTrue();
    }

    @Test
    void shouldNotAllowArchiveFromArchivedOrRemoved() {
        assertThat(productWithStatus(ProductStatus.ARCHIVED).canArchive()).isFalse();
        assertThat(productWithStatus(ProductStatus.REMOVED).canArchive()).isFalse();
    }

    private Product productWithStatus(ProductStatus status) {
        return new Product(
                UUID.randomUUID(),
                sellerId,
                UUID.randomUUID(),
                "Item",
                status,
                Instant.now()
        );
    }
}
