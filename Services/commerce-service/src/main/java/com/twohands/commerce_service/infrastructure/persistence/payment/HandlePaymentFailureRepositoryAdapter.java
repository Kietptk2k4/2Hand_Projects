package com.twohands.commerce_service.infrastructure.persistence.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.application.order.common.InventoryReleasedOutboxService;
import com.twohands.commerce_service.application.order.common.OrderCancelledOutboxService;
import com.twohands.commerce_service.application.payment.common.PaymentCancelledOutboxService;
import com.twohands.commerce_service.application.payment.common.PaymentFailedOutboxService;
import com.twohands.commerce_service.application.order.common.PaymentExpiredOutboxService;
import com.twohands.commerce_service.domain.order.OrderItemQuantity;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.payment.HandlePaymentFailureRepository;
import com.twohands.commerce_service.domain.payment.HandlePaymentFailureResult;
import com.twohands.commerce_service.domain.payment.LockedPaymentContext;
import com.twohands.commerce_service.domain.payment.PaymentFailureOutcome;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class HandlePaymentFailureRepositoryAdapter implements HandlePaymentFailureRepository {

    private static final Logger log = LoggerFactory.getLogger(HandlePaymentFailureRepositoryAdapter.class);

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final OutboxEventRepository outboxEventRepository;
    private final PaymentFailedOutboxService paymentFailedOutboxService;
    private final PaymentCancelledOutboxService paymentCancelledOutboxService;
    private final PaymentExpiredOutboxService paymentExpiredOutboxService;
    private final OrderCancelledOutboxService orderCancelledOutboxService;
    private final InventoryReleasedOutboxService inventoryReleasedOutboxService;
    private final ObjectMapper objectMapper;

    public HandlePaymentFailureRepositoryAdapter(
            NamedParameterJdbcTemplate jdbcTemplate,
            OutboxEventRepository outboxEventRepository,
            PaymentFailedOutboxService paymentFailedOutboxService,
            PaymentCancelledOutboxService paymentCancelledOutboxService,
            PaymentExpiredOutboxService paymentExpiredOutboxService,
            OrderCancelledOutboxService orderCancelledOutboxService,
            InventoryReleasedOutboxService inventoryReleasedOutboxService,
            ObjectMapper objectMapper
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.outboxEventRepository = outboxEventRepository;
        this.paymentFailedOutboxService = paymentFailedOutboxService;
        this.paymentCancelledOutboxService = paymentCancelledOutboxService;
        this.paymentExpiredOutboxService = paymentExpiredOutboxService;
        this.orderCancelledOutboxService = orderCancelledOutboxService;
        this.inventoryReleasedOutboxService = inventoryReleasedOutboxService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<LockedPaymentContext> lockPaymentById(UUID paymentId) {
        return lockPayment("p.id = :paymentId", new MapSqlParameterSource("paymentId", paymentId));
    }

    @Override
    public Optional<LockedPaymentContext> lockPaymentByPayosOrderCode(String payosOrderCode) {
        return lockPayment(
                "p.payos_order_code = :payosOrderCode",
                new MapSqlParameterSource("payosOrderCode", payosOrderCode)
        );
    }

    @Override
    public HandlePaymentFailureResult handleFailure(
            LockedPaymentContext payment,
            PaymentStatus terminalStatus,
            String reason,
            String changedBy,
            String historyPayloadJson,
            Instant occurredAt
    ) {
        if (payment.isPaid()) {
            return result(payment, PaymentFailureOutcome.SKIPPED_ALREADY_PAID, terminalStatus, false, null);
        }

        if (payment.isCod()) {
            log.info("Skipping payment failure for COD payment {}", payment.paymentId());
            return result(payment, PaymentFailureOutcome.SKIPPED_COD, terminalStatus, false, null);
        }

        if (!payment.isPending()) {
            return result(payment, PaymentFailureOutcome.SKIPPED_PAYMENT_NOT_PENDING, terminalStatus, false, null);
        }

        if (!isCancellableOrderStatus(payment.orderStatus()) || !PaymentStatus.PENDING.name().equals(payment.orderPaymentStatus())) {
            log.warn(
                    "Order {} is no longer cancellable for payment failure (orderStatus={}, orderPaymentStatus={})",
                    payment.orderId(),
                    payment.orderStatus(),
                    payment.orderPaymentStatus()
            );
            return result(payment, PaymentFailureOutcome.SKIPPED_ORDER_NOT_CANCELLABLE, terminalStatus, false, null);
        }

        if (hasShipmentBlockingCancel(payment.orderId())) {
            log.info("Skipping payment failure for order {} because shipment has started", payment.orderId());
            return result(payment, PaymentFailureOutcome.SKIPPED_SHIPMENT_STARTED, terminalStatus, false, null);
        }

        List<OrderItemQuantity> pendingItems = findPendingOrderItems(payment.orderId());
        releaseInventoryOrThrow(pendingItems, payment.orderId(), occurredAt);

        int paymentUpdated = markPaymentTerminal(payment.paymentId(), terminalStatus, historyPayloadJson, occurredAt);
        if (paymentUpdated == 0) {
            return result(payment, PaymentFailureOutcome.SKIPPED_PAYMENT_NOT_PENDING, terminalStatus, false, null);
        }

        int orderUpdated = cancelOrder(payment.orderId(), terminalStatus, occurredAt);
        if (orderUpdated == 0) {
            return result(payment, PaymentFailureOutcome.SKIPPED_ORDER_NOT_CANCELLABLE, terminalStatus, false, null);
        }

        cancelPendingOrderItems(payment.orderId(), occurredAt);
        insertOrderStatusHistory(payment.orderId(), payment.orderStatus(), changedBy, reason, occurredAt);
        insertPaymentStatusHistory(
                payment.paymentId(),
                payment.paymentStatus(),
                terminalStatus,
                historyPayloadJson,
                occurredAt
        );

        publishOutboxEvents(payment, terminalStatus, reason, pendingItems, occurredAt);

        return result(payment, PaymentFailureOutcome.PROCESSED, terminalStatus, !pendingItems.isEmpty(), occurredAt);
    }

    private Optional<LockedPaymentContext> lockPayment(String paymentPredicate, MapSqlParameterSource params) {
        String sql = """
                SELECT p.id AS payment_id,
                       p.order_id,
                       o.buyer_id,
                       p.status::text AS payment_status,
                       p.payment_method::text AS payment_method,
                       o.status::text AS order_status,
                       o.payment_status::text AS order_payment_status
                FROM payments p
                INNER JOIN orders o ON o.id = p.order_id
                WHERE %s
                FOR UPDATE OF p, o
                """.formatted(paymentPredicate);
        List<LockedPaymentContext> rows = jdbcTemplate.query(sql, params, this::mapLockedPayment);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    private boolean hasShipmentBlockingCancel(UUID orderId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM shipments
                    WHERE order_id = :orderId
                      AND status NOT IN ('PENDING', 'CANCELLED')
                )
                """;
        Boolean exists = jdbcTemplate.queryForObject(
                sql,
                new MapSqlParameterSource("orderId", orderId),
                Boolean.class
        );
        return Boolean.TRUE.equals(exists);
    }

    private List<OrderItemQuantity> findPendingOrderItems(UUID orderId) {
        String sql = """
                SELECT id, product_id, quantity
                FROM order_items
                WHERE order_id = :orderId AND status = 'PENDING'
                """;
        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("orderId", orderId),
                (rs, rowNum) -> new OrderItemQuantity(
                        UUID.fromString(rs.getString("id")),
                        UUID.fromString(rs.getString("product_id")),
                        rs.getInt("quantity")
                )
        );
    }

    private void releaseInventoryOrThrow(List<OrderItemQuantity> items, UUID orderId, Instant now) {
        String sql = """
                UPDATE product_inventories
                SET reserved_quantity = reserved_quantity - :quantity,
                    stock_quantity = stock_quantity + :quantity,
                    updated_at = :now
                WHERE product_id = :productId
                  AND reserved_quantity >= :quantity
                """;
        for (OrderItemQuantity item : items) {
            int updated = jdbcTemplate.update(sql, new MapSqlParameterSource()
                    .addValue("productId", item.productId())
                    .addValue("quantity", item.quantity())
                    .addValue("now", Timestamp.from(now)));
            if (updated == 0) {
                log.error(
                        "Failed to release inventory for order {} product {} quantity {}",
                        orderId,
                        item.productId(),
                        item.quantity()
                );
                throw new AppException(
                        ErrorCode.INTERNAL_ERROR,
                        "Inventory release conflict for order " + orderId
                );
            }
        }
    }

    private int markPaymentTerminal(
            UUID paymentId,
            PaymentStatus terminalStatus,
            String providerResponseJson,
            Instant now
    ) {
        String sql = """
                UPDATE payments
                SET status = CAST(:terminalStatus AS payment_status),
                    provider_response = COALESCE(CAST(:providerResponse AS jsonb), provider_response),
                    updated_at = :now
                WHERE id = :paymentId
                  AND status = 'PENDING'
                """;
        return jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("paymentId", paymentId)
                .addValue("terminalStatus", terminalStatus.name())
                .addValue("providerResponse", providerResponseJson)
                .addValue("now", Timestamp.from(now)));
    }

    private int cancelOrder(UUID orderId, PaymentStatus terminalStatus, Instant now) {
        String sql = """
                UPDATE orders
                SET status = 'CANCELLED',
                    payment_status = CAST(:terminalStatus AS payment_status),
                    updated_at = :now
                WHERE id = :orderId
                  AND status IN ('CREATED', 'AWAITING_PAYMENT')
                  AND payment_status = 'PENDING'
                """;
        return jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("orderId", orderId)
                .addValue("terminalStatus", terminalStatus.name())
                .addValue("now", Timestamp.from(now)));
    }

    private void cancelPendingOrderItems(UUID orderId, Instant now) {
        String sql = """
                UPDATE order_items
                SET status = 'CANCELLED', updated_at = :now
                WHERE order_id = :orderId AND status = 'PENDING'
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("orderId", orderId)
                .addValue("now", Timestamp.from(now)));
    }

    private void insertOrderStatusHistory(
            UUID orderId,
            String oldStatus,
            String changedBy,
            String note,
            Instant now
    ) {
        String sql = """
                INSERT INTO order_status_history(id, order_id, old_status, new_status, changed_by, note, created_at)
                VALUES (:id, :orderId, CAST(:oldStatus AS order_status), 'CANCELLED', :changedBy, :note, :createdAt)
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", UUID.randomUUID())
                .addValue("orderId", orderId)
                .addValue("oldStatus", oldStatus)
                .addValue("changedBy", changedBy)
                .addValue("note", note)
                .addValue("createdAt", Timestamp.from(now)));
    }

    private void insertPaymentStatusHistory(
            UUID paymentId,
            String oldStatus,
            PaymentStatus newStatus,
            String payloadJson,
            Instant now
    ) {
        String sql = """
                INSERT INTO payment_status_history(id, payment_id, old_status, new_status, payload, created_at)
                VALUES (
                    :id, :paymentId,
                    CAST(:oldStatus AS payment_status),
                    CAST(:newStatus AS payment_status),
                    CAST(:payload AS jsonb),
                    :createdAt
                )
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", UUID.randomUUID())
                .addValue("paymentId", paymentId)
                .addValue("oldStatus", oldStatus)
                .addValue("newStatus", newStatus.name())
                .addValue("payload", payloadJson != null ? payloadJson : defaultHistoryPayload(now))
                .addValue("createdAt", Timestamp.from(now)));
    }

    private void publishOutboxEvents(
            LockedPaymentContext payment,
            PaymentStatus terminalStatus,
            String reason,
            List<OrderItemQuantity> pendingItems,
            Instant occurredAt
    ) {
        switch (terminalStatus) {
            case FAILED -> outboxEventRepository.save(
                    paymentFailedOutboxService.build(
                            payment.paymentId(),
                            payment.orderId(),
                            payment.buyerId(),
                            reason,
                            occurredAt
                    )
            );
            case CANCELLED -> outboxEventRepository.save(
                    paymentCancelledOutboxService.build(payment.paymentId(), payment.orderId(), reason, occurredAt)
            );
            case EXPIRED -> outboxEventRepository.save(
                    paymentExpiredOutboxService.build(payment.paymentId(), payment.orderId(), occurredAt)
            );
            default -> throw new AppException(ErrorCode.INTERNAL_ERROR, "Unsupported terminal payment status");
        }

        outboxEventRepository.save(orderCancelledOutboxService.build(payment.orderId(), reason, occurredAt));
        if (!pendingItems.isEmpty()) {
            outboxEventRepository.save(inventoryReleasedOutboxService.build(payment.orderId(), pendingItems, occurredAt));
        }
    }

    private String defaultHistoryPayload(Instant now) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("processed_at", now.toString());
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Cannot serialize payment history payload", ex);
        }
    }

    private boolean isCancellableOrderStatus(String status) {
        return "CREATED".equals(status) || "AWAITING_PAYMENT".equals(status);
    }

    private LockedPaymentContext mapLockedPayment(ResultSet rs, int rowNum) throws SQLException {
        return new LockedPaymentContext(
                UUID.fromString(rs.getString("payment_id")),
                UUID.fromString(rs.getString("order_id")),
                UUID.fromString(rs.getString("buyer_id")),
                rs.getString("payment_status"),
                rs.getString("payment_method"),
                rs.getString("order_status"),
                rs.getString("order_payment_status")
        );
    }

    private HandlePaymentFailureResult result(
            LockedPaymentContext payment,
            PaymentFailureOutcome outcome,
            PaymentStatus terminalStatus,
            boolean inventoryReleased,
            Instant processedAt
    ) {
        return new HandlePaymentFailureResult(
                outcome,
                payment.paymentId(),
                payment.orderId(),
                terminalStatus,
                inventoryReleased,
                processedAt
        );
    }
}
