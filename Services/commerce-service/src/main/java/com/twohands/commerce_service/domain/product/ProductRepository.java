package com.twohands.commerce_service.domain.product;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {

    Optional<Product> findById(UUID productId);

    Product save(Product product);
}
