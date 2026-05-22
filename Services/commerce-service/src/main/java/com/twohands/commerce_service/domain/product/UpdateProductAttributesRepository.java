package com.twohands.commerce_service.domain.product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UpdateProductAttributesRepository {

    Optional<UpdateProductAttributesProductRef> findProductByIdAndSellerId(UUID productId, UUID sellerId);

    List<ProductAttributeItem> replaceAttributes(UUID productId, List<ProductAttributeItem> attributes);
}
