package com.twohands.commerce_service.infrastructure.persistence.order;

import com.twohands.commerce_service.domain.order.CreateOrderItemResult;
import com.twohands.commerce_service.domain.order.CreateOrderLineRequest;
import com.twohands.commerce_service.domain.order.CreateOrderRepository;
import com.twohands.commerce_service.domain.order.CreateOrderRequest;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class CreateOrderRepositoryAdapter implements CreateOrderRepository {

    private static final String CHANGED_BY = "BUYER";
    private static final String CREATE_ORDER_NOTE = "CREATE_ORDER";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public CreateOrderRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<CreateOrderItemResult> createOrder(CreateOrderRequest request, OrderStatus initialStatus) {
        Instant now = request.occurredAt();
        insertOrder(
                request.orderId(),
                request.buyerId(),
                request.totalAmount(),
                request.finalAmount(),
                request.paymentMethod(),
                initialStatus,
                now
        );
        List<CreateOrderItemResult> items = insertOrderItems(request.orderId(), request.lines(), now);
        insertOrderStatusHistory(request.orderId(), null, initialStatus.name(), now);
        return items;
    }

    private void insertOrder(
            UUID orderId,
            UUID buyerId,
            java.math.BigDecimal totalAmount,
            java.math.BigDecimal finalAmount,
            PaymentMethod paymentMethod,
            OrderStatus orderStatus,
            Instant now
    ) {
        String sql = """
                INSERT INTO orders(
                    id, buyer_id, total_amount, final_amount, payment_method,
                    status, payment_status, created_at, updated_at
                ) VALUES (
                    :orderId, :buyerId, :totalAmount, :finalAmount, CAST(:paymentMethod AS payment_method),
                    CAST(:status AS order_status), 'PENDING', :now, :now
                )
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("orderId", orderId)
                .addValue("buyerId", buyerId)
                .addValue("totalAmount", totalAmount)
                .addValue("finalAmount", finalAmount)
                .addValue("paymentMethod", paymentMethod.name())
                .addValue("status", orderStatus.name())
                .addValue("now", Timestamp.from(now)));
    }

    private List<CreateOrderItemResult> insertOrderItems(UUID orderId, List<CreateOrderLineRequest> lines, Instant now) {
        String sql = """
                INSERT INTO order_items(
                    id, order_id, product_id, seller_id, quantity,
                    unit_price_snapshot, final_price, sku_snapshot, product_name_snapshot,
                    image_snapshot, attributes_snapshot, shipping_fee_allocated, shop_name_snapshot,
                    status, created_at, updated_at
                ) VALUES (
                    :id, :orderId, :productId, :sellerId, :quantity,
                    :unitPrice, :finalPrice, :sku, :productName,
                    :imageUrl, CAST(:attributesJson AS jsonb), :shippingFeeAllocated, :shopName,
                    'PENDING', :now, :now
                )
                """;
        List<CreateOrderItemResult> results = new ArrayList<>();
        for (CreateOrderLineRequest line : lines) {
            UUID orderItemId = UUID.randomUUID();
            jdbcTemplate.update(sql, new MapSqlParameterSource()
                    .addValue("id", orderItemId)
                    .addValue("orderId", orderId)
                    .addValue("productId", line.productId())
                    .addValue("sellerId", line.sellerId())
                    .addValue("quantity", line.quantity())
                    .addValue("unitPrice", line.unitPrice())
                    .addValue("finalPrice", line.lineTotal())
                    .addValue("sku", line.sku())
                    .addValue("productName", line.productName())
                    .addValue("imageUrl", line.imageUrl())
                    .addValue("attributesJson", line.attributesJson() == null ? "{}" : line.attributesJson())
                    .addValue("shippingFeeAllocated", line.shippingFeeAllocated())
                    .addValue("shopName", line.shopName())
                    .addValue("now", Timestamp.from(now)));
            results.add(new CreateOrderItemResult(
                    orderItemId,
                    line.productId(),
                    line.sellerId(),
                    line.quantity(),
                    line.unitPrice(),
                    line.lineTotal()
            ));
        }
        return results;
    }

    private void insertOrderStatusHistory(UUID orderId, String oldStatus, String newStatus, Instant now) {
        String sql = """
                INSERT INTO order_status_history(id, order_id, old_status, new_status, changed_by, note, created_at)
                VALUES (
                    :id, :orderId,
                    CAST(:oldStatus AS order_status),
                    CAST(:newStatus AS order_status),
                    :changedBy, :note, :createdAt
                )
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", UUID.randomUUID())
                .addValue("orderId", orderId)
                .addValue("oldStatus", oldStatus)
                .addValue("newStatus", newStatus)
                .addValue("changedBy", CHANGED_BY)
                .addValue("note", CREATE_ORDER_NOTE)
                .addValue("createdAt", Timestamp.from(now)));
    }
}
