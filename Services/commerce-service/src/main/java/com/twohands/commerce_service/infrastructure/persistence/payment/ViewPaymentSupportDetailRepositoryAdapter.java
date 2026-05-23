package com.twohands.commerce_service.infrastructure.persistence.payment;

import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.payment.PaymentStatusHistoryEntry;
import com.twohands.commerce_service.domain.payment.PaymentSupportDetailSnapshot;
import com.twohands.commerce_service.domain.payment.PaymentWebhookSummary;
import com.twohands.commerce_service.domain.payment.ViewPaymentSupportDetailRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ViewPaymentSupportDetailRepositoryAdapter implements ViewPaymentSupportDetailRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ViewPaymentSupportDetailRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<PaymentSupportDetailSnapshot> findByPaymentId(UUID paymentId) {
        PaymentHeaderRow payment = loadPayment(paymentId);
        if (payment == null) {
            return Optional.empty();
        }

        List<PaymentStatusHistoryEntry> timeline = loadPaymentTimeline(paymentId);
        List<PaymentWebhookSummary> webhookEvents = StringUtils.hasText(payment.payosOrderCode())
                ? loadWebhookEvents(payment.payosOrderCode())
                : List.of();

        return Optional.of(new PaymentSupportDetailSnapshot(
                payment.paymentId(),
                payment.orderId(),
                payment.payerId(),
                payment.paymentMethod(),
                payment.amount(),
                payment.currency(),
                payment.status(),
                payment.paidAt(),
                payment.expiredAt(),
                payment.createdAt(),
                payment.updatedAt(),
                payment.payosOrderCode(),
                payment.payosTransactionId(),
                payment.payosCheckoutUrl(),
                payment.checkoutUrlExpiredAt(),
                payment.orderStatus(),
                payment.orderPaymentStatus(),
                timeline,
                webhookEvents
        ));
    }

    private PaymentHeaderRow loadPayment(UUID paymentId) {
        String sql = """
                SELECT p.id AS payment_id,
                       p.order_id,
                       p.payer_id,
                       p.payment_method::text AS payment_method,
                       p.amount,
                       p.currency,
                       p.status::text AS payment_status,
                       p.paid_at,
                       p.expired_at,
                       p.created_at,
                       p.updated_at,
                       p.payos_order_code,
                       p.payos_transaction_id,
                       p.payos_checkout_url,
                       p.checkout_url_expired_at,
                       o.status::text AS order_status,
                       o.payment_status::text AS order_payment_status
                FROM payments p
                INNER JOIN orders o ON o.id = p.order_id
                WHERE p.id = :paymentId
                """;
        List<PaymentHeaderRow> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("paymentId", paymentId),
                (rs, rowNum) -> mapPaymentHeader(rs)
        );
        return rows.isEmpty() ? null : rows.getFirst();
    }

    private List<PaymentStatusHistoryEntry> loadPaymentTimeline(UUID paymentId) {
        String sql = """
                SELECT old_status::text AS old_status,
                       new_status::text AS new_status,
                       created_at
                FROM payment_status_history
                WHERE payment_id = :paymentId
                ORDER BY created_at ASC
                """;
        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("paymentId", paymentId),
                (rs, rowNum) -> new PaymentStatusHistoryEntry(
                        optionalPaymentStatus(rs.getString("old_status")),
                        PaymentStatus.valueOf(rs.getString("new_status")),
                        rs.getTimestamp("created_at").toInstant()
                )
        );
    }

    private List<PaymentWebhookSummary> loadWebhookEvents(String payosOrderCode) {
        String sql = """
                SELECT provider,
                       event_type,
                       signature_valid,
                       processed,
                       created_at
                FROM payment_webhook_logs
                WHERE payos_order_code = :payosOrderCode
                ORDER BY created_at DESC
                """;
        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("payosOrderCode", payosOrderCode),
                (rs, rowNum) -> new PaymentWebhookSummary(
                        rs.getString("provider"),
                        rs.getString("event_type"),
                        rs.getBoolean("signature_valid"),
                        rs.getBoolean("processed"),
                        rs.getTimestamp("created_at").toInstant()
                )
        );
    }

    private PaymentHeaderRow mapPaymentHeader(ResultSet rs) throws SQLException {
        return new PaymentHeaderRow(
                UUID.fromString(rs.getString("payment_id")),
                UUID.fromString(rs.getString("order_id")),
                UUID.fromString(rs.getString("payer_id")),
                PaymentMethod.valueOf(rs.getString("payment_method")),
                rs.getBigDecimal("amount"),
                rs.getString("currency"),
                PaymentStatus.valueOf(rs.getString("payment_status")),
                optionalInstant(rs.getTimestamp("paid_at")),
                optionalInstant(rs.getTimestamp("expired_at")),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant(),
                rs.getString("payos_order_code"),
                rs.getString("payos_transaction_id"),
                rs.getString("payos_checkout_url"),
                optionalInstant(rs.getTimestamp("checkout_url_expired_at")),
                OrderStatus.valueOf(rs.getString("order_status")),
                PaymentStatus.valueOf(rs.getString("order_payment_status"))
        );
    }

    private PaymentStatus optionalPaymentStatus(String value) {
        return value == null ? null : PaymentStatus.valueOf(value);
    }

    private Instant optionalInstant(Timestamp value) {
        return value == null ? null : value.toInstant();
    }

    private record PaymentHeaderRow(
            UUID paymentId,
            UUID orderId,
            UUID payerId,
            PaymentMethod paymentMethod,
            BigDecimal amount,
            String currency,
            PaymentStatus status,
            Instant paidAt,
            Instant expiredAt,
            Instant createdAt,
            Instant updatedAt,
            String payosOrderCode,
            String payosTransactionId,
            String payosCheckoutUrl,
            Instant checkoutUrlExpiredAt,
            OrderStatus orderStatus,
            PaymentStatus orderPaymentStatus
    ) {
    }
}
