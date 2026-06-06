package com.twohands.commerce_service.domain.product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UpdateProductMediaRepository {

    Optional<UpdateProductMediaProductRef> findProductByIdAndSellerId(UUID productId, UUID sellerId);

    List<ProductMediaItem> replaceMedia(UUID productId, List<ProductMediaItem> mediaItems);
}
