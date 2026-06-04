package com.twohands.commerce_service.infrastructure.outbox;

import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CommerceOutboxTopicResolver {

    private static final Map<String, String> EVENT_TYPE_TO_TOPIC = Map.ofEntries(
            Map.entry("COMMERCE_ORDER_CREATED", "commerce.order.created"),
            Map.entry("COMMERCE_SELLER_ORDER_ITEM_PROCESSING", "commerce.seller_order_item.processing"),
            Map.entry("COMMERCE_ORDER_CANCELLED", "commerce.order.cancelled"),
            Map.entry("COMMERCE_ORDER_COMPLETED", "commerce.order.completed"),
            Map.entry("COMMERCE_PAYMENT_CREATED", "commerce.payment.created"),
            Map.entry("COMMERCE_PAYMENT_PAID", "commerce.payment.paid"),
            Map.entry("COMMERCE_PAYMENT_FAILED", "commerce.payment.failed"),
            Map.entry("COMMERCE_PAYMENT_CANCELLED", "commerce.payment.cancelled"),
            Map.entry("COMMERCE_PAYMENT_EXPIRED", "commerce.payment.expired"),
            Map.entry("COMMERCE_SHIPMENT_CREATED", "commerce.shipment.created"),
            Map.entry("COMMERCE_SHIPMENT_STATUS_CHANGED", "commerce.shipment.status_changed"),
            Map.entry("COMMERCE_SHIPMENT_SHIPPED", "commerce.shipment.shipped"),
            Map.entry("COMMERCE_SHIPMENT_DELIVERED", "commerce.shipment.delivered"),
            Map.entry("COMMERCE_INVENTORY_RESERVED", "commerce.inventory.reserved"),
            Map.entry("COMMERCE_INVENTORY_RELEASED", "commerce.inventory.released"),
            Map.entry("COMMERCE_PRODUCT_CREATED", "commerce.product.created"),
            Map.entry("COMMERCE_PRODUCT_UPDATED", "commerce.product.updated"),
            Map.entry("COMMERCE_PRODUCT_ATTRIBUTES_UPDATED", "commerce.product.attributes.updated"),
            Map.entry("COMMERCE_PRODUCT_INVENTORY_UPDATED", "commerce.product.inventory.updated"),
            Map.entry("COMMERCE_PRODUCT_PRICE_UPDATED", "commerce.product.price.updated"),
            Map.entry("COMMERCE_PRODUCT_PUBLISHED", "commerce.product.published"),
            Map.entry("COMMERCE_PRODUCT_PAUSED", "commerce.product.paused"),
            Map.entry("COMMERCE_PRODUCT_ARCHIVED", "commerce.product.archived"),
            Map.entry("COMMERCE_PRODUCT_REMOVED", "commerce.product.removed"),
            Map.entry("COMMERCE_REVIEW_CREATED", "commerce.review.created"),
            Map.entry("COMMERCE_REVIEW_UPDATED", "commerce.review.updated"),
            Map.entry("COMMERCE_REVIEW_REPLIED", "commerce.review.replied"),
            Map.entry("COMMERCE_REVIEW_HIDDEN", "commerce.review.hidden"),
            Map.entry("COMMERCE_REVIEW_RESTORED", "commerce.review.restored"),
            Map.entry("COMMERCE_SHOP_CREATED", "commerce.shop.created"),
            Map.entry("COMMERCE_SHOP_UPDATED", "commerce.shop.updated"),
            Map.entry("COMMERCE_SHOP_VACATION_UPDATED", "commerce.shop.vacation.updated"),
            Map.entry("COMMERCE_SHOP_SUSPENDED", "commerce.shop.suspended"),
            Map.entry("COMMERCE_SHOP_CLOSED", "commerce.shop.closed"),
            Map.entry("COMMERCE_SHOP_RESTORED", "commerce.shop.restored")
    );

    public String resolve(String eventType) {
        String topic = EVENT_TYPE_TO_TOPIC.get(eventType);
        if (topic == null) {
            throw new AppException(
                    ErrorCode.INTERNAL_ERROR,
                    "Unsupported outbox event type for publish: " + eventType
            );
        }
        return topic;
    }
}
