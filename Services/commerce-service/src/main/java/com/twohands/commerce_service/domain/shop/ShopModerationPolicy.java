package com.twohands.commerce_service.domain.shop;

import java.util.Map;
import java.util.Set;

public final class ShopModerationPolicy {

    private static final Map<ShopModerationAction, ShopStatus> TARGET_STATUS = Map.of(
            ShopModerationAction.SUSPEND, ShopStatus.SUSPENDED,
            ShopModerationAction.CLOSE, ShopStatus.CLOSED,
            ShopModerationAction.RESTORE, ShopStatus.ACTIVE
    );

    private static final Map<ShopStatus, Set<ShopStatus>> ALLOWED_TARGETS = Map.of(
            ShopStatus.ACTIVE, Set.of(ShopStatus.SUSPENDED, ShopStatus.CLOSED),
            ShopStatus.SUSPENDED, Set.of(ShopStatus.ACTIVE, ShopStatus.CLOSED),
            ShopStatus.CLOSED, Set.of(ShopStatus.ACTIVE)
    );

    private ShopModerationPolicy() {
    }

    public static ShopStatus targetStatus(ShopModerationAction action) {
        return TARGET_STATUS.get(action);
    }

    public static boolean canTransition(ShopStatus current, ShopStatus target) {
        if (current == target) {
            return true;
        }
        return ALLOWED_TARGETS.getOrDefault(current, Set.of()).contains(target);
    }

    public static boolean shouldInvalidateCartItems(ShopModerationAction action, boolean alreadyModerated) {
        return !alreadyModerated && (action == ShopModerationAction.SUSPEND || action == ShopModerationAction.CLOSE);
    }
}
