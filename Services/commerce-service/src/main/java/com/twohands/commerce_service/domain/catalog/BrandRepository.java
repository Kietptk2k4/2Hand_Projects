package com.twohands.commerce_service.domain.catalog;

import java.util.UUID;

public interface BrandRepository {

    boolean existsActiveById(UUID brandId);
}