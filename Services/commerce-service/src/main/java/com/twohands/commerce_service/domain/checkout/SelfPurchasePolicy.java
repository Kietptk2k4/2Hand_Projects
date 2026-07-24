package com.twohands.commerce_service.domain.checkout;

import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;

import java.util.UUID;

public final class SelfPurchasePolicy {

    private SelfPurchasePolicy() {
    }

    public static void assertNotOwnListing(UUID buyerId, UUID sellerId) {
        if (buyerId == null || sellerId == null) {
            return;
        }
        if (buyerId.equals(sellerId)) {
            throw new AppException(
                    ErrorCode.SELF_PURCHASE,
                    "You cannot purchase your own product"
            );
        }
    }
}
