package com.twohands.commerce_service.domain.catalog;

import java.util.UUID;

public interface ProductCategoryRepository {

    boolean existsActiveById(UUID categoryId);
}
