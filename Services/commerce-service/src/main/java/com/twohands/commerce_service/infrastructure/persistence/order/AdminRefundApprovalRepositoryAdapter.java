package com.twohands.commerce_service.infrastructure.persistence.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.application.order.common.InventoryReleasedOutboxService;
import com.twohands.commerce_service.application.order.common.OrderCancelledOutboxService;
import com.twohands.commerce_service.application.payment.common.PaymentRefundedOutboxService;
import com.twohands.commerce_service.domain.inventory.ReserveInventoryRepository;
import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.order.AdminRefundApprovalItem;
import com.twohands.commerce_service.domain.order.AdminRefundApprovalRepository;
import com.twohands.commerce_service.domain.order.OrderItemQuantity;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentRefundRequestStatus;
import com.twohands.commerce_service.domain.payment.PaymentRefundRequestedBy;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
public class AdminRefundApprovalRepositoryAdapter implements AdminRefundApprovalRepository {

    private static final Logger log = LoggerFactory.getLogger(AdminRefundApprovalRepositoryAdapter.class);
    private static final String DEFAULT_CONFIRM_REASON = "ADMIN_REFUND_CONFIRMED";
    private static final String DEFAULT_REJECT_REASON = "ADMIN_REFUND_REJECTED";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final OutboxEventRepository outboxEventRepository;
    private final PaymentRefundedOutboxService paymentRefundedOutboxService;
    private final OrderCancelledOutboxService orderCancelledOutboxService;
    private final InventoryReleasedOutboxService inventoryReleasedOutboxService;
    private final ReserveInventoryRepository reserveInventoryRepository;
    private final ObjectMapper objectMapper;

    public AdminRefundApprovalRepositoryAdapter(
            NamedParameterJdbcTemplate jdbcTemplate,
            OutboxEventRepository outboxEventRepository,
            PaymentRefundedOutboxService paymentRefundedOutboxService,
            OrderCancelledOutboxService orderCancelledOutboxService,
            InventoryReleasedOutboxService inventoryReleasedOutboxService,
            ReserveInventoryRepository reserveInventoryRepository,
            ObjectMapper objectMapper
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.outboxEventRepository = outboxEventRepository;
        this.paymentRefundedOutboxService = paymentRefundedOutboxService;
        this.orderCancelledOutboxService = orderCancelledOutboxService;
        this.inventoryReleasedOutboxService = inventoryReleasedOutboxService;
        this.reserveInventoryRepository = reserveInventoryRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public long countAdminRefundApprovals(Optional<PaymentRefundRequestStatus> status) {
        String sql = """
                SELECT COUNT(*)
                FROM payment_refund_requests r
                WHERE (:status IS NULL OR r.status = CAST(:status AS payment_refund_request_status))
                """;
        Long count = jdbcTemplate.queryForObject(sql, statusParams(status), Long.class);
        return count == null ? 0L : count;
    }

    @Override
    public List<AdminRefundApprovalItem> findAdminRefundApprovals(
            Optional<PaymentRefundRequestStatus> status,
            PageQuery pageQuery
    ) {
        String sql = """
                SELECT r.id, r.payment_id, r.order_id, o.buyer_id,
                       r.requested_by::text AS requested_by, r.requested_by_user_id,
                       r.status::text AS status, r.amount, r.reason, r.admin_note,
                       o.payment_method::text AS payment_method,
                       o.payment_status::text AS order_payment_status,
                       o.status::text AS order_status,
                       r.created_at, r.confirmed_at, r.rejected_at
                FROM payment_refund_requests r
                JOIN orders o ON o.id = r.order_id
                WHERE (:status IS NULL OR r.status = CAST(:status AS payment_refund_request_status))
                ORDER BY r.created_at DESC
                LIMIT :limit OFFSET :offset
                """;
        MapSqlParameterSource params = statusParams(status)
                .addValue("limit", pageQuery.limit())
                .addValue("offset", pageQuery.offset());
        return jdbcTemplate.query(sql, params, (rs, rowNum) -> mapItem(rs));
    }

    @Override
    public Optional<AdminRefundApprovalItem> findById(UUID refundRequestId) {
        String sql = """
                SELECT r.id, r.payment_id, r.order_id, o.buyer_id,
                       r.requested_by::text AS requested_by, r.requested_by_user_id,
                       r.status::text AS status, r.amount, r.reason, r.admin_note,
                       o.payment_method::text AS payment_method,
                       o.payment_status::text AS order_payment_status,
                       o.status::text AS order_status,
                       r.created_at, r.confirmed_at, r.rejected_at
                FROM payment_refund_requests r
                JOIN orders o ON o.id = r.order_id
                WHERE r.id = :refundRequestId
                """;
        List<AdminRefundApprovalItem> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("refundRequestId", refundRequestId),
                (rs, rowNum) -> mapItem(rs)
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    @Transactional
    public AdminRefundApprovalItem confirmRefund(UUID refundRequestId, String adminNote, Instant now) {
        RefundRow refund = lockRefundRequest(refundRequestId);
        if (refund == null) {
            throw new AppException(ErrorCode.REFUND_REQUEST_NOT_FOUND);
        }
        if (!PaymentRefundRequestStatus.REQUESTED.name().equals(refund.status)) {
            throw new AppException(ErrorCode.INVALID_REFUND_REQUEST_STATE);
        }

        OrderRow order = lockOrder(refund.orderId);
        PaymentRow payment = lockPayment(refund.paymentId);
        if (order == null || payment == null) {
            throw new AppException(ErrorCode.REFUND_REQUEST_NOT_FOUND);
        }
        if (!PaymentStatus.PAID.name().equals(payment.status)
                || !OrderStatus.PROCESSING.name().equals(order.status)) {
            throw new AppException(ErrorCode.INVALID_REFUND_REQUEST_STATE);
        }

        String resolvedAdminNote = resolveNote(adminNote);
        List<OrderItemQuantity> releasableItems = findReleasableOrderItems(order.id);
        releaseInventoryOrThrow(releasableItems, order.id, now);
        syncProductStatusesAfterRelease(releasableItems, now);

        int refundUpdated = confirmRefundRequest(refundRequestId, resolvedAdminNote, now);
        if (refundUpdated == 0) {
            throw new AppException(ErrorCode.INVALID_REFUND_REQUEST_STATE);
        }

        int paymentUpdated = markPaymentRefunded(payment.id, now);
        if (paymentUpdated == 0) {
            throw new AppException(ErrorCode.INVALID_REFUND_REQUEST_STATE);
        }

        int orderUpdated = cancelOrderAfterRefund(order.id, now);
        if (orderUpdated == 0) {
            throw new AppException(ErrorCode.INVALID_REFUND_REQUEST_STATE);
        }

        cancelReleasableOrderItems(order.id, now);
        insertOrderStatusHistory(order.id, order.status, DEFAULT_CONFIRM_REASON, now, "ADMIN");
        insertPaymentStatusHistory(payment.id, payment.status, DEFAULT_CONFIRM_REASON, now, "ADMIN");

        outboxEventRepository.save(paymentRefundedOutboxService.build(
                refundRequestId,
                payment.id,
                order.id,
                order.buyerId,
                refund.amount,
                resolvedAdminNote,
                now
        ));
        outboxEventRepository.save(orderCancelledOutboxService.build(
                order.id(),
                order.buyerId(),
                findDistinctSellerIds(order.id()),
                resolveCancellationReason(refund.reason()),
                "ADMIN",
                null,
                refund.requestedBy(),
                now
        ));
        if (!releasableItems.isEmpty()) {
            outboxEventRepository.save(inventoryReleasedOutboxService.build(order.id(), releasableItems, now));
        }

        return findById(refundRequestId).orElseThrow(() -> new AppException(ErrorCode.REFUND_REQUEST_NOT_FOUND));
    }

    @Override
    @Transactional
    public AdminRefundApprovalItem rejectRefund(UUID refundRequestId, String adminNote, Instant now) {
        RefundRow refund = lockRefundRequest(refundRequestId);
        if (refund == null) {
            throw new AppException(ErrorCode.REFUND_REQUEST_NOT_FOUND);
        }
        if (!PaymentRefundRequestStatus.REQUESTED.name().equals(refund.status)) {
            throw new AppException(ErrorCode.INVALID_REFUND_REQUEST_STATE);
        }

        String resolvedAdminNote = resolveNote(adminNote);
        int updated = jdbcTemplate.update("""
                        UPDATE payment_refund_requests
                        SET status = CAST(:status AS payment_refund_request_status),
                            admin_note = :adminNote,
                            rejected_at = :now,
                            updated_at = :now
                        WHERE id = :refundRequestId AND status = 'REQUESTED'
                        """,
                new MapSqlParameterSource()
                        .addValue("status", PaymentRefundRequestStatus.REJECTED.name())
                        .addValue("adminNote", resolvedAdminNote)
                        .addValue("now", Timestamp.from(now))
                        .addValue("refundRequestId", refundRequestId)
        );
        if (updated == 0) {
            throw new AppException(ErrorCode.INVALID_REFUND_REQUEST_STATE);
        }

        return findById(refundRequestId).orElseThrow(() -> new AppException(ErrorCode.REFUND_REQUEST_NOT_FOUND));
    }

    private RefundRow lockRefundRequest(UUID refundRequestId) {
        String sql = """
                SELECT id, payment_id, order_id, status::text AS status, amount, reason,
                       requested_by::text AS requested_by
                FROM payment_refund_requests
                WHERE id = :refundRequestId
                FOR UPDATE
                """;
        List<RefundRow> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("refundRequestId", refundRequestId),
                (rs, rowNum) -> new RefundRow(
                        UUID.fromString(rs.getString("id")),
                        UUID.fromString(rs.getString("payment_id")),
                        UUID.fromString(rs.getString("order_id")),
                        rs.getString("status"),
                        rs.getBigDecimal("amount"),
                        rs.getString("reason"),
                        rs.getString("requested_by")
                )
        );
        return rows.isEmpty() ? null : rows.getFirst();
    }

    private OrderRow lockOrder(UUID orderId) {
        String sql = """
                SELECT id, buyer_id, status, payment_status
                FROM orders
                WHERE id = :orderId
                FOR UPDATE
                """;
        List<OrderRow> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("orderId", orderId),
                (rs, rowNum) -> new OrderRow(
                        UUID.fromString(rs.getString("id")),
                        UUID.fromString(rs.getString("buyer_id")),
                        rs.getString("status"),
                        rs.getString("payment_status")
                )
        );
        return rows.isEmpty() ? null : rows.getFirst();
    }

    private PaymentRow lockPayment(UUID paymentId) {
        String sql = """
                SELECT id, status
                FROM payments
                WHERE id = :paymentId
                FOR UPDATE
                """;
        List<PaymentRow> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("paymentId", paymentId),
                (rs, rowNum) -> new PaymentRow(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("status")
                )
        );
        return rows.isEmpty() ? null : rows.getFirst();
    }

    private int confirmRefundRequest(UUID refundRequestId, String adminNote, Instant now) {
        return jdbcTemplate.update("""
                        UPDATE payment_refund_requests
                        SET status = CAST(:status AS payment_refund_request_status),
                            admin_note = :adminNote,
                            confirmed_at = :now,
                            updated_at = :now
                        WHERE id = :refundRequestId AND status = 'REQUESTED'
                        """,
                new MapSqlParameterSource()
                        .addValue("status", PaymentRefundRequestStatus.CONFIRMED.name())
                        .addValue("adminNote", adminNote)
                        .addValue("now", Timestamp.from(now))
                        .addValue("refundRequestId", refundRequestId)
        );
    }

    private int markPaymentRefunded(UUID paymentId, Instant now) {
        return jdbcTemplate.update("""
                        UPDATE payments
                        SET status = 'REFUNDED', updated_at = :now
                        WHERE id = :paymentId AND status = 'PAID'
                        """,
                new MapSqlParameterSource()
                        .addValue("paymentId", paymentId)
                        .addValue("now", Timestamp.from(now))
        );
    }

    private int cancelOrderAfterRefund(UUID orderId, Instant now) {
        return jdbcTemplate.update("""
                        UPDATE orders
                        SET status = 'CANCELLED',
                            payment_status = 'REFUNDED',
                            updated_at = :now
                        WHERE id = :orderId
                          AND status = 'PROCESSING'
                          AND payment_status = 'PAID'
                        """,
                new MapSqlParameterSource()
                        .addValue("orderId", orderId)
                        .addValue("now", Timestamp.from(now))
        );
    }

    private void cancelReleasableOrderItems(UUID orderId, Instant now) {
        jdbcTemplate.update("""
                        UPDATE order_items
                        SET status = 'CANCELLED', updated_at = :now
                        WHERE order_id = :orderId AND status IN ('PENDING', 'PROCESSING')
                        """,
                new MapSqlParameterSource()
                        .addValue("orderId", orderId)
                        .addValue("now", Timestamp.from(now))
        );
    }

    private List<OrderItemQuantity> findReleasableOrderItems(UUID orderId) {
        return jdbcTemplate.query("""
                        SELECT id, product_id, quantity
                        FROM order_items
                        WHERE order_id = :orderId AND status IN ('PENDING', 'PROCESSING')
                        """,
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
                        "Failed to release inventory for refund order {} product {} quantity {}",
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

    private void syncProductStatusesAfterRelease(List<OrderItemQuantity> items, Instant now) {
        if (items.isEmpty()) {
            return;
        }
        List<UUID> productIds = items.stream()
                .map(OrderItemQuantity::productId)
                .distinct()
                .toList();
        reserveInventoryRepository.syncInStockProductStatuses(productIds, now);
    }

    private void insertOrderStatusHistory(
            UUID orderId,
            String oldStatus,
            String reason,
            Instant now,
            String changedBy
    ) {
        jdbcTemplate.update("""
                        INSERT INTO order_status_history(id, order_id, old_status, new_status, changed_by, note, created_at)
                        VALUES (:id, :orderId, CAST(:oldStatus AS order_status), 'CANCELLED', :changedBy, :note, :createdAt)
                        """,
                new MapSqlParameterSource()
                        .addValue("id", UUID.randomUUID())
                        .addValue("orderId", orderId)
                        .addValue("oldStatus", oldStatus)
                        .addValue("changedBy", changedBy)
                        .addValue("note", reason)
                        .addValue("createdAt", Timestamp.from(now))
        );
    }

    private void insertPaymentStatusHistory(
            UUID paymentId,
            String oldStatus,
            String reason,
            Instant now,
            String changedBy
    ) {
        jdbcTemplate.update("""
                        INSERT INTO payment_status_history(id, payment_id, old_status, new_status, payload, created_at)
                        VALUES (:id, :paymentId, CAST(:oldStatus AS payment_status), 'REFUNDED',
                                CAST(:payload AS jsonb), :createdAt)
                        """,
                new MapSqlParameterSource()
                        .addValue("id", UUID.randomUUID())
                        .addValue("paymentId", paymentId)
                        .addValue("oldStatus", oldStatus)
                        .addValue("payload", buildPaymentHistoryPayload(reason, now, changedBy))
                        .addValue("createdAt", Timestamp.from(now))
        );
    }

    private String buildPaymentHistoryPayload(String reason, Instant now, String changedBy) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("reason", reason);
        payload.put("refunded_by", changedBy);
        payload.put("processed_at", now.toString());
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Cannot serialize payment history payload", ex);
        }
    }

    private MapSqlParameterSource statusParams(Optional<PaymentRefundRequestStatus> status) {
        return new MapSqlParameterSource()
                .addValue("status", status.map(PaymentRefundRequestStatus::name).orElse(null));
    }

    private String resolveNote(String adminNote) {
        if (adminNote == null || adminNote.isBlank()) {
            return "";
        }
        return adminNote.trim();
    }

    private String resolveCancellationReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return DEFAULT_CONFIRM_REASON;
        }
        return reason.trim();
    }

    private List<UUID> findDistinctSellerIds(UUID orderId) {
        String sql = """
                SELECT DISTINCT seller_id
                FROM order_items
                WHERE order_id = :orderId
                ORDER BY seller_id
                """;
        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("orderId", orderId),
                (rs, rowNum) -> UUID.fromString(rs.getString("seller_id"))
        );
    }

    private AdminRefundApprovalItem mapItem(ResultSet rs) throws SQLException {
        return new AdminRefundApprovalItem(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("payment_id")),
                UUID.fromString(rs.getString("order_id")),
                UUID.fromString(rs.getString("buyer_id")),
                PaymentRefundRequestedBy.valueOf(rs.getString("requested_by")),
                UUID.fromString(rs.getString("requested_by_user_id")),
                PaymentRefundRequestStatus.valueOf(rs.getString("status")),
                rs.getBigDecimal("amount"),
                rs.getString("reason"),
                rs.getString("admin_note"),
                PaymentMethod.valueOf(rs.getString("payment_method")),
                PaymentStatus.valueOf(rs.getString("order_payment_status")),
                OrderStatus.valueOf(rs.getString("order_status")),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("confirmed_at") == null ? null : rs.getTimestamp("confirmed_at").toInstant(),
                rs.getTimestamp("rejected_at") == null ? null : rs.getTimestamp("rejected_at").toInstant()
        );
    }

    private record RefundRow(
            UUID id,
            UUID paymentId,
            UUID orderId,
            String status,
            BigDecimal amount,
            String reason,
            String requestedBy
    ) {
    }

    private record OrderRow(UUID id, UUID buyerId, String status, String paymentStatus) {
    }

    private record PaymentRow(UUID id, String status) {
    }
}
