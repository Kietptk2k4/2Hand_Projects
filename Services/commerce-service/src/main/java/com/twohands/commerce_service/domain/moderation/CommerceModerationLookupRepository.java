package com.twohands.commerce_service.domain.moderation;

import java.util.Optional;
import java.util.UUID;

public interface CommerceModerationLookupRepository {

    Optional<ProductModerationOwner> findProductOwner(UUID productId);

    Optional<ShopModerationOwner> findShopOwner(UUID shopId);

    Optional<ReviewModerationParties> findReviewParties(UUID reviewId);
}
