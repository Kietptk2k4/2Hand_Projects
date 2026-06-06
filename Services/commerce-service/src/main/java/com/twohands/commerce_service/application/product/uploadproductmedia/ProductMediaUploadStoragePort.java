package com.twohands.commerce_service.application.product.uploadproductmedia;

import java.time.Instant;
import java.util.UUID;

public interface ProductMediaUploadStoragePort {

    ProductMediaUploadIntent createUploadIntent(
            UUID sellerId,
            UUID productId,
            String contentType,
            String mediaKind,
            Instant expiresAt
    );
}
