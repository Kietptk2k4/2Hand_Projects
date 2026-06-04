package com.twohands.commerce_service.infrastructure.persistence.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.application.order.common.PaymentPaidOutboxService;
import com.twohands.commerce_service.domain.order.CompleteOrderOutcome;
import com.twohands.commerce_service.domain.order.CompleteOrderResult;
import com.twohands.commerce_service.domain.order.ConfirmOrderReceivedRepository;
import com.twohands.commerce_service.domain.order.ConfirmOrderReceivedResult;
import com.twohands.commerce_service.domain.order.DeliveredOrderCompletionResult;
import com.twohands.commerce_service.domain.order.DeliveredOrderCompletionRepository;
import com.twohands.commerce_service.domain.order.OrderCompletionRepository;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.order.StaleDeliveredOrderItemCandidate;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class DeliveredOrderCompletionRepositoryAdapter
        implements DeliveredOrderCompletionRepository, ConfirmOrderReceivedRepository {

    private static final Logger log = LoggerFactory.getLogger(DeliveredOrderCompletionRepositoryAdapter.class);
    private static final String AUTO_COMPLETE_REASON = "AUTO_COMPLETE_DELIVERED_ORDER";
    private static final String AUTO_CHANGED_BY = "SYSTEM:AUTO_COMPLETE_DELIVERED_ORDER";
    private static final String CONFIRM_REASON = "BUYER_CONFIRM_RECEIVED";
    private static final String CONFIRM_CHANGED_BY = "BUYER";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final OutboxEventRepository outboxEventRepository;
    private final PaymentPaidOutboxService paymentPaidOutboxService;
    private final OrderCompletionRepository orderCompletionRepository;
    private final ObjectMapper objectMapper;

    public DeliveredOrderCompletionRepositoryAdapter(
            NamedParameterJdbcTemplate jdbcTemplate,
            OutboxEventRepository outboxEventRepository,
            PaymentPaidOutboxService paymentPaidOutboxService,
            OrderCompletionRepository orderCompletionRepository,
            ObjectMapper objectMapper
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.outboxEventRepository = outboxEventRepository;
        this.paymentPaidOutboxService = paymentPaidOutboxService;
        this.orderCompletionRepository = orderCompletionRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<StaleDeliveredOrderItemCandidate> findStaleDeliveredItems(int batchSize, Instant deliveredBefore) {
        String sql = """
                SELECT oi.id AS order_item_id, oi.order_id
                FROM order_items oi
                LEFT JOIN shipments s ON s.id = oi.shipment_id
                WHERE oi.status = 'DELIVERED'
                  AND COALESCE(s.delivered_at, oi.updated_at) < :deliveredBefore
                ORDER BY COALESCE(s.delivered_at, oi.updated_at) ASC
                LIMIT :batchSize
                """;
        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("deliveredBefore", Timestamp.from(deliveredBefore))
                        .addValue("batchSize", batchSize),
                (rs, rowNum) -> new StaleDeliveredOrderItemCandidate(
                        UUID.fromString(rs.getString("order_item_id")),
                        UUID.fromString(rs.getString("order_id"))
                )
        );
    }

    @Override
    @Transactional
    public DeliveredOrderCompletionResult completeDeliveredItemsForOrder(
            UUID orderId,
            List<UUID> orderItemIds,
            Instant now
    ) {
        OrderRow order = lockOrder(orderId);
        if (order == null) {
            return DeliveredOrderCompletionResult.noOp();
        }

        if ("COMPLETED".equals(order.status)) {
            return DeliveredOrderCompletionResult.noOp();
        }

        return processDeliveredItemCompletion(
                order,
                orderItemIds,
                now,
                new CompletionContext(AUTO_COMPLETE_REASON, AUTO_CHANGED_BY, "SYSTEM")
        );
    }

    @Override
    @Transactional
    public ConfirmOrderReceivedResult confirmReceivedByBuyer(UUID buyerId, UUID orderId, Instant now) {
        OrderRow order = lockOrderForBuyer(orderId, buyerId);
        if (order == null) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }

        if ("CANCELLED".equals(order.status)) {
            throw new AppException(ErrorCode.ORDER_NOT_CANCELLABLE, "Cancelled order cannot be confirmed");
        }

        if ("COMPLETED".equals(order.status)) {
            return toConfirmResult(order, 0, false, false, true, now);
        }

        List<UUID> deliveredItemIds = findDeliveredOrderItemIds(orderId);
        if (deliveredItemIds.isEmpty()) {
            throw new AppException(ErrorCode.ORDER_ITEMS_NOT_DELIVERED);
        }

        validatePaymentStateForConfirm(orderId);

        DeliveredOrderCompletionResult completion = processDeliveredItemCompletion(
                order,
                deliveredItemIds,
                now,
                new CompletionContext(CONFIRM_REASON, CONFIRM_CHANGED_BY, "BUYER")
        );

        if (completion.itemsCompleted() == 0) {
            throw new AppException(ErrorCode.ORDER_ITEMS_NOT_DELIVERED);
        }

        OrderRow updatedOrder = lockOrder(orderId);
        PaymentRow payment = lockPaymentByOrderId(orderId);
        return toConfirmResult(
                updatedOrder,
                completion.itemsCompleted(),
                completion.paymentMarkedPaid(),
                completion.orderCompleted(),
                completion.skippedAlreadyCompleted(),
                payment == null ? PaymentStatus.PENDING.name() : payment.status
        );
    }

    private DeliveredOrderCompletionResult processDeliveredItemCompletion(
            OrderRow order,
            List<UUID> orderItemIds,
            Instant now,
            CompletionContext context
    ) {
        if ("COMPLETED".equals(order.status)) {
            return DeliveredOrderCompletionResult.noOp();
        }

        int itemsCompleted = 0;
        for (UUID orderItemId : orderItemIds) {
            itemsCompleted += completeOrderItem(orderItemId, order.id, now);
        }

        if (itemsCompleted == 0) {
            return new DeliveredOrderCompletionResult(0, false, false, false);
        }

        PaymentRow payment = lockPaymentByOrderId(order.id);
        boolean paymentMarkedPaid = false;
        if (payment != null && "COD".equals(payment.paymentMethod) && "PENDING".equals(payment.status)) {
            paymentMarkedPaid = markCodPaymentPaid(payment, order, context.reason(), now);
            if (paymentMarkedPaid) {
                syncOrderPaymentStatusPaid(order.id, now);
            }
        }

        CompleteOrderResult completion = orderCompletionRepository.completeIfEligible(
                order.id,
                context.reason(),
                context.changedBy(),
                context.completedByOutbox(),
                now
        );
        boolean orderCompleted = completion.outcome() == CompleteOrderOutcome.COMPLETED;

        return new DeliveredOrderCompletionResult(
                itemsCompleted,
                orderCompleted,
                paymentMarkedPaid,
                completion.outcome() == CompleteOrderOutcome.ALREADY_COMPLETED
        );
    }

    private void validatePaymentStateForConfirm(UUID orderId) {
        PaymentRow payment = lockPaymentByOrderId(orderId);
        if (payment == null) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_STATE);
        }
        if ("COD".equals(payment.paymentMethod)) {
            if (!"PENDING".equals(payment.status) && !"PAID".equals(payment.status)) {
                throw new AppException(ErrorCode.INVALID_PAYMENT_STATE);
            }
            return;
        }
        if ("PAYOS".equals(payment.paymentMethod) && !"PAID".equals(payment.status)) {
            throw new AppException(
                    ErrorCode.INVALID_PAYMENT_STATE,
                    "PayOS order must be paid before confirming receipt"
            );
        }
    }

    private List<UUID> findDeliveredOrderItemIds(UUID orderId) {
        String sql = """
                SELECT id
                FROM order_items
                WHERE order_id = :orderId AND status = 'DELIVERED'
                """;
        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("orderId", orderId),
                (rs, rowNum) -> UUID.fromString(rs.getString("id"))
        );
    }

    private OrderRow lockOrderForBuyer(UUID orderId, UUID buyerId) {
        String sql = """
                SELECT id, status, payment_status, buyer_id
                FROM orders
                WHERE id = :orderId AND buyer_id = :buyerId
                FOR UPDATE
                """;
        List<OrderRow> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("orderId", orderId)
                        .addValue("buyerId", buyerId),
                (rs, rowNum) -> mapOrderRow(rs)
        );
        return rows.isEmpty() ? null : rows.getFirst();
    }

    private ConfirmOrderReceivedResult toConfirmResult(
            OrderRow order,
            int itemsCompleted,
            boolean paymentMarkedPaid,
            boolean orderCompleted,
            boolean alreadyCompleted,
            String paymentStatus
    ) {
        return new ConfirmOrderReceivedResult(
                order.id,
                OrderStatus.valueOf(order.status),
                PaymentStatus.valueOf(paymentStatus != null ? paymentStatus : order.paymentStatus),
                itemsCompleted,
                paymentMarkedPaid,
                orderCompleted,
                alreadyCompleted
        );
    }

    private ConfirmOrderReceivedResult toConfirmResult(
            OrderRow order,
            int itemsCompleted,
            boolean paymentMarkedPaid,
            boolean orderCompleted,
            boolean alreadyCompleted,
            Instant now
    ) {
        PaymentRow payment = lockPaymentByOrderId(order.id);
        String status = payment != null ? payment.status : order.paymentStatus;
        return toConfirmResult(order, itemsCompleted, paymentMarkedPaid, orderCompleted, alreadyCompleted, status);
    }

    private OrderRow lockOrder(UUID orderId) {
        String sql = """
                SELECT id, status, payment_status, buyer_id
                FROM orders
                WHERE id = :orderId
                FOR UPDATE
                """;
        List<OrderRow> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("orderId", orderId),
                (rs, rowNum) -> mapOrderRow(rs)
        );
        return rows.isEmpty() ? null : rows.getFirst();
    }

    private PaymentRow lockPaymentByOrderId(UUID orderId) {
        String sql = """
                SELECT id, order_id, status, payment_method
                FROM payments
                WHERE order_id = :orderId
                FOR UPDATE
                """;
        List<PaymentRow> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("orderId", orderId),
                (rs, rowNum) -> mapPaymentRow(rs)
        );
        return rows.isEmpty() ? null : rows.getFirst();
    }

    private int completeOrderItem(UUID orderItemId, UUID orderId, Instant now) {
        String sql = """
                UPDATE order_items
                SET status = 'COMPLETED',
                    completed_at = :now,
                    updated_at = :now
                WHERE id = :orderItemId
                  AND order_id = :orderId
                  AND status = 'DELIVERED'
                """;
        return jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("orderItemId", orderItemId)
                .addValue("orderId", orderId)
                .addValue("now", Timestamp.from(now)));
    }

    private boolean markCodPaymentPaid(PaymentRow payment, OrderRow order, String reason, Instant now) {
        String sql = """
                UPDATE payments
                SET status = 'PAID',
                    paid_at = :now,
                    updated_at = :now
                WHERE id = :paymentId AND status = 'PENDING'
                """;
        int updated = jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("paymentId", payment.id)
                .addValue("now", Timestamp.from(now)));
        if (updated == 0) {
            return false;
        }

        insertPaymentStatusHistory(payment.id, payment.status, reason, now);
        outboxEventRepository.save(paymentPaidOutboxService.build(
                payment.id,
                payment.orderId,
                order.buyerId,
                reason,
                now,
                order.id.toString()
        ));
        return true;
    }

    private void syncOrderPaymentStatusPaid(UUID orderId, Instant now) {
        String sql = """
                UPDATE orders
                SET payment_status = 'PAID', updated_at = :now
                WHERE id = :orderId AND payment_status = 'PENDING'
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("orderId", orderId)
                .addValue("now", Timestamp.from(now)));
    }

    private void insertPaymentStatusHistory(UUID paymentId, String oldStatus, String reason, Instant now) {
        String sql = """
                INSERT INTO payment_status_history(id, payment_id, old_status, new_status, payload, created_at)
                VALUES (:id, :paymentId, CAST(:oldStatus AS payment_status), 'PAID', CAST(:payload AS jsonb), :createdAt)
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", UUID.randomUUID())
                .addValue("paymentId", paymentId)
                .addValue("oldStatus", oldStatus)
                .addValue("payload", buildPaymentHistoryPayload(reason, now))
                .addValue("createdAt", Timestamp.from(now)));
    }

    private String buildPaymentHistoryPayload(String reason, Instant now) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("reason", reason);
        payload.put("processed_at", now.toString());
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Cannot serialize payment history payload", ex);
        }
    }

    private OrderRow mapOrderRow(ResultSet rs) throws SQLException {
        return new OrderRow(
                UUID.fromString(rs.getString("id")),
                rs.getString("status"),
                rs.getString("payment_status"),
                UUID.fromString(rs.getString("buyer_id"))
        );
    }

    private PaymentRow mapPaymentRow(ResultSet rs) throws SQLException {
        return new PaymentRow(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("order_id")),
                rs.getString("status"),
                rs.getString("payment_method")
        );
    }

    private record OrderRow(UUID id, String status, String paymentStatus, UUID buyerId) {
    }

    private record PaymentRow(UUID id, UUID orderId, String status, String paymentMethod) {
    }

    private record CompletionContext(String reason, String changedBy, String completedByOutbox) {
    }
}
