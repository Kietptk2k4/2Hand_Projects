package com.twohands.commerce_service.domain.product;

import java.time.Instant;

public interface CreateProductRepository {

    CreateProductResult create(CreateProductDraft draft, Instant occurredAt);
}
