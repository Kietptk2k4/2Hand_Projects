package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.order.OrderItemStatus;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SellerOrderListEntryResponse(
        @JsonProperty("order_item_id") UUID orderItemId,
        @JsonProperty("order_id") UUID orderId,
        @JsonProperty("product_id") UUID productId,
        int quantity,
        @JsonProperty("unit_price_snapshot") BigDecimal unitPriceSnapshot,
        @JsonProperty("final_price") BigDecimal finalPrice,
        @JsonProperty("shipping_fee_allocated") BigDecimal shippingFeeAllocated,
        @JsonProperty("product_name_snapshot") String productNameSnapshot,
        @JsonProperty("image_snapshot") String imageSnapshot,
        @JsonProperty("item_status") OrderItemStatus itemStatus,
        @JsonProperty("item_created_at") Instant itemCreatedAt,
        @JsonProperty("item_updated_at") Instant itemUpdatedAt,
        @JsonProperty("order_status") OrderStatus orderStatus,
        @JsonProperty("order_payment_status") PaymentStatus orderPaymentStatus,
        @JsonProperty("order_payment_method") PaymentMethod orderPaymentMethod,
        @JsonProperty("order_created_at") Instant orderCreatedAt,
        SellerOrderListPaymentSummaryResponse payment,
        @JsonProperty("shipment_summary") SellerOrderListShipmentSummaryResponse shipmentSummary
) {
}
