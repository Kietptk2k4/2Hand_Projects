package com.twohands.commerce_service.application.shop.uploadshopmedia;

import java.time.Instant;
import java.util.UUID;

public interface ShopMediaUploadStoragePort {

    ShopMediaUploadIntent createUploadIntent(
            UUID sellerId,
            String contentType,
            String mediaKind,
            Instant expiresAt
    );
}
