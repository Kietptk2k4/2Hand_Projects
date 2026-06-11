package com.twohands.social_service.unit.application.post.common;

import com.twohands.social_service.application.post.common.ProductTagSnapshotResolver;
import com.twohands.social_service.domain.integration.CommerceProductCatalogClient;
import com.twohands.social_service.domain.integration.CommerceProductSnapshot;
import com.twohands.social_service.domain.post.ProductTag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductTagSnapshotResolverTest {

    private final CommerceProductCatalogClient commerceProductCatalogClient = mock(CommerceProductCatalogClient.class);
    private final ProductTagSnapshotResolver resolver = new ProductTagSnapshotResolver(commerceProductCatalogClient);

    @Test
    void shouldAttachSnapshotWhenProductVisible() {
        String productId = UUID.randomUUID().toString();
        when(commerceProductCatalogClient.findVisibleProductSnapshot(productId))
                .thenReturn(Optional.of(new CommerceProductSnapshot(
                        productId,
                        "iPhone 14",
                        "https://cdn/phone.jpg",
                        "Mobile"
                )));

        List<ProductTag> resolved = resolver.resolve(List.of(new ProductTag(productId, new BigDecimal("19900000"))));

        assertThat(resolved).hasSize(1);
        assertThat(resolved.getFirst().name()).isEqualTo("iPhone 14");
        assertThat(resolved.getFirst().imageUrl()).isEqualTo("https://cdn/phone.jpg");
        assertThat(resolved.getFirst().category()).isEqualTo("Mobile");
        assertThat(resolved.getFirst().isAvailable()).isTrue();
    }

    @Test
    void shouldMarkUnavailableWhenProductNotVisible() {
        String productId = UUID.randomUUID().toString();
        when(commerceProductCatalogClient.findVisibleProductSnapshot(productId)).thenReturn(Optional.empty());

        List<ProductTag> resolved = resolver.resolve(List.of(new ProductTag(productId, new BigDecimal("100000"))));

        assertThat(resolved).hasSize(1);
        assertThat(resolved.getFirst().isAvailable()).isFalse();
    }
}
