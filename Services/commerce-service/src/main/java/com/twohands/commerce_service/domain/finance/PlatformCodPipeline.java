package com.twohands.commerce_service.domain.finance;

public record PlatformCodPipeline(
        SellerRevenueBucket inTransit,
        SellerRevenueBucket pendingConfirm,
        SellerRevenueBucket recognized
) {
}
