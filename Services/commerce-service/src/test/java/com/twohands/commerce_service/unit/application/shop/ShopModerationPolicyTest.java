package com.twohands.commerce_service.unit.application.shop;

import com.twohands.commerce_service.domain.shop.ShopModerationAction;
import com.twohands.commerce_service.domain.shop.ShopModerationPolicy;
import com.twohands.commerce_service.domain.shop.ShopStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShopModerationPolicyTest {

    @Test
    void mapsActionsToTargetStatus() {
        assertThat(ShopModerationPolicy.targetStatus(ShopModerationAction.SUSPEND))
                .isEqualTo(ShopStatus.SUSPENDED);
        assertThat(ShopModerationPolicy.targetStatus(ShopModerationAction.CLOSE))
                .isEqualTo(ShopStatus.CLOSED);
        assertThat(ShopModerationPolicy.targetStatus(ShopModerationAction.RESTORE))
                .isEqualTo(ShopStatus.ACTIVE);
    }

    @Test
    void allowsDocumentedTransitions() {
        assertThat(ShopModerationPolicy.canTransition(ShopStatus.ACTIVE, ShopStatus.SUSPENDED)).isTrue();
        assertThat(ShopModerationPolicy.canTransition(ShopStatus.ACTIVE, ShopStatus.CLOSED)).isTrue();
        assertThat(ShopModerationPolicy.canTransition(ShopStatus.SUSPENDED, ShopStatus.ACTIVE)).isTrue();
        assertThat(ShopModerationPolicy.canTransition(ShopStatus.CLOSED, ShopStatus.ACTIVE)).isTrue();
    }

    @Test
    void rejectsInvalidTransition() {
        assertThat(ShopModerationPolicy.canTransition(ShopStatus.SUSPENDED, ShopStatus.SUSPENDED)).isTrue();
        assertThat(ShopModerationPolicy.canTransition(ShopStatus.CLOSED, ShopStatus.SUSPENDED)).isFalse();
    }
}
