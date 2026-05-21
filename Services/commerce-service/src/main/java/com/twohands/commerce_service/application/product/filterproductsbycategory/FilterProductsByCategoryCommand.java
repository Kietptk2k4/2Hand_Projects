package com.twohands.commerce_service.application.product.filterproductsbycategory;

import java.util.UUID;

public record FilterProductsByCategoryCommand(
        UUID categoryId,
        Integer page,
        Integer limit,
        String sort,
        Boolean includeChildren
) {
}
