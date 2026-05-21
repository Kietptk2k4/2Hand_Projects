package com.twohands.commerce_service.application.product.archiveproduct;

import java.util.UUID;

public record ArchiveProductCommand(UUID sellerId, UUID productId) {
}
