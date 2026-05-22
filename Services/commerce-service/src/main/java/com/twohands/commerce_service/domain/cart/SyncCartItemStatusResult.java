package com.twohands.commerce_service.domain.cart;

public record SyncCartItemStatusResult(
        int candidatesScanned,
        int updated,
        int unchanged,
        int skipped
) {
    public static SyncCartItemStatusResult empty() {
        return new SyncCartItemStatusResult(0, 0, 0, 0);
    }
}
