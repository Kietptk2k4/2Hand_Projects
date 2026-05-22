package com.twohands.commerce_service.domain.product;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UpdateProductPriceRepository {

    Optional<UpdateProductPriceProductRef> findProductByIdAndSellerId(UUID productId, UUID sellerId);

    List<OverlappingProductPrice> findOverlappingPrices(UUID productId, Instant startAt, Instant endAt);

    int closePricesAtStart(UUID productId, List<UUID> priceIds, Instant endAt);

    ProductPriceRecord insertPrice(UpdateProductPriceDraft draft, Instant createdAt);
}
