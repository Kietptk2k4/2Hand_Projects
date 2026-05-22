package com.twohands.commerce_service.unit.infrastructure.outbox;

import com.twohands.commerce_service.application.order.common.InventoryReleasedOutboxService;
import com.twohands.commerce_service.application.order.common.InventoryReservedOutboxService;
import com.twohands.commerce_service.application.order.common.OrderCancelledOutboxService;
import com.twohands.commerce_service.application.order.common.OrderCompletedOutboxService;
import com.twohands.commerce_service.application.order.common.OrderCreatedOutboxService;
import com.twohands.commerce_service.application.order.common.PaymentPaidOutboxService;
import com.twohands.commerce_service.application.order.common.SellerOrderItemProcessingOutboxService;
import com.twohands.commerce_service.application.payment.common.PaymentCancelledOutboxService;
import com.twohands.commerce_service.application.payment.common.PaymentCreatedOutboxService;
import com.twohands.commerce_service.application.payment.common.PaymentFailedOutboxService;
import com.twohands.commerce_service.application.product.common.ProductArchivedOutboxService;
import com.twohands.commerce_service.application.product.common.ProductCreatedOutboxService;
import com.twohands.commerce_service.application.product.common.ProductPausedOutboxService;
import com.twohands.commerce_service.application.product.common.ProductPublishedOutboxService;
import com.twohands.commerce_service.application.product.common.ProductRemovedOutboxService;
import com.twohands.commerce_service.application.product.common.ProductUpdatedOutboxService;
import com.twohands.commerce_service.application.review.common.ReviewCreatedOutboxService;
import com.twohands.commerce_service.application.review.common.ReviewHiddenOutboxService;
import com.twohands.commerce_service.application.review.common.ReviewRepliedOutboxService;
import com.twohands.commerce_service.application.review.common.ReviewRestoredOutboxService;
import com.twohands.commerce_service.application.shipment.common.ShipmentCreatedOutboxService;
import com.twohands.commerce_service.application.shipment.common.ShipmentStatusChangedOutboxService;
import com.twohands.commerce_service.application.shop.common.ShopClosedOutboxService;
import com.twohands.commerce_service.application.shop.common.ShopCreatedOutboxService;
import com.twohands.commerce_service.application.shop.common.ShopRestoredOutboxService;
import com.twohands.commerce_service.application.shop.common.ShopSuspendedOutboxService;
import com.twohands.commerce_service.infrastructure.outbox.CommerceOutboxTopicResolver;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommerceOutboxTopicResolverTest {

    private final CommerceOutboxTopicResolver resolver = new CommerceOutboxTopicResolver();

    @Test
    void shouldResolveAllKnownCommerceEventTypes() {
        assertThat(resolver.resolve(OrderCreatedOutboxService.EVENT_TYPE))
                .isEqualTo("commerce.order.created");
        assertThat(resolver.resolve(SellerOrderItemProcessingOutboxService.EVENT_TYPE))
                .isEqualTo("commerce.seller_order_item.processing");
        assertThat(resolver.resolve(OrderCancelledOutboxService.EVENT_TYPE))
                .isEqualTo("commerce.order.cancelled");
        assertThat(resolver.resolve(OrderCompletedOutboxService.EVENT_TYPE))
                .isEqualTo("commerce.order.completed");
        assertThat(resolver.resolve(PaymentCreatedOutboxService.EVENT_TYPE))
                .isEqualTo("commerce.payment.created");
        assertThat(resolver.resolve(PaymentPaidOutboxService.EVENT_TYPE))
                .isEqualTo("commerce.payment.paid");
        assertThat(resolver.resolve(PaymentFailedOutboxService.EVENT_TYPE))
                .isEqualTo("commerce.payment.failed");
        assertThat(resolver.resolve(PaymentCancelledOutboxService.EVENT_TYPE))
                .isEqualTo("commerce.payment.cancelled");
        assertThat(resolver.resolve(ShipmentCreatedOutboxService.EVENT_TYPE))
                .isEqualTo("commerce.shipment.created");
        assertThat(resolver.resolve(ShipmentStatusChangedOutboxService.EVENT_TYPE))
                .isEqualTo("commerce.shipment.status_changed");
        assertThat(resolver.resolve(InventoryReservedOutboxService.EVENT_TYPE))
                .isEqualTo("commerce.inventory.reserved");
        assertThat(resolver.resolve(InventoryReleasedOutboxService.EVENT_TYPE))
                .isEqualTo("commerce.inventory.released");
        assertThat(resolver.resolve(ProductCreatedOutboxService.EVENT_TYPE))
                .isEqualTo("commerce.product.created");
        assertThat(resolver.resolve(ProductUpdatedOutboxService.EVENT_TYPE))
                .isEqualTo("commerce.product.updated");
        assertThat(resolver.resolve(ProductPublishedOutboxService.EVENT_TYPE))
                .isEqualTo("commerce.product.published");
        assertThat(resolver.resolve(ProductPausedOutboxService.EVENT_TYPE))
                .isEqualTo("commerce.product.paused");
        assertThat(resolver.resolve(ProductArchivedOutboxService.EVENT_TYPE))
                .isEqualTo("commerce.product.archived");
        assertThat(resolver.resolve(ProductRemovedOutboxService.EVENT_TYPE))
                .isEqualTo("commerce.product.removed");
        assertThat(resolver.resolve(ReviewCreatedOutboxService.EVENT_TYPE))
                .isEqualTo("commerce.review.created");
        assertThat(resolver.resolve(ReviewRepliedOutboxService.EVENT_TYPE))
                .isEqualTo("commerce.review.replied");
        assertThat(resolver.resolve(ReviewHiddenOutboxService.EVENT_TYPE))
                .isEqualTo("commerce.review.hidden");
        assertThat(resolver.resolve(ReviewRestoredOutboxService.EVENT_TYPE))
                .isEqualTo("commerce.review.restored");
        assertThat(resolver.resolve(ShopCreatedOutboxService.EVENT_TYPE))
                .isEqualTo("commerce.shop.created");
        assertThat(resolver.resolve(ShopSuspendedOutboxService.EVENT_TYPE))
                .isEqualTo("commerce.shop.suspended");
        assertThat(resolver.resolve(ShopClosedOutboxService.EVENT_TYPE))
                .isEqualTo("commerce.shop.closed");
        assertThat(resolver.resolve(ShopRestoredOutboxService.EVENT_TYPE))
                .isEqualTo("commerce.shop.restored");
    }
}
