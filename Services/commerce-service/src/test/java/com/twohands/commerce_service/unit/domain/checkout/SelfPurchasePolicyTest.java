package com.twohands.commerce_service.unit.domain.checkout;

import com.twohands.commerce_service.domain.checkout.SelfPurchasePolicy;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SelfPurchasePolicyTest {

    @Test
    void assertNotOwnListing_rejectsWhenBuyerEqualsSeller() {
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> SelfPurchasePolicy.assertNotOwnListing(userId, userId))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SELF_PURCHASE);
    }

    @Test
    void assertNotOwnListing_allowsDifferentBuyerAndSeller() {
        assertThatCode(() -> SelfPurchasePolicy.assertNotOwnListing(UUID.randomUUID(), UUID.randomUUID()))
                .doesNotThrowAnyException();
    }

    @Test
    void assertNotOwnListing_ignoresNullIds() {
        UUID id = UUID.randomUUID();
        assertThatCode(() -> SelfPurchasePolicy.assertNotOwnListing(null, id)).doesNotThrowAnyException();
        assertThatCode(() -> SelfPurchasePolicy.assertNotOwnListing(id, null)).doesNotThrowAnyException();
        assertThatCode(() -> SelfPurchasePolicy.assertNotOwnListing(null, null)).doesNotThrowAnyException();
    }
}
