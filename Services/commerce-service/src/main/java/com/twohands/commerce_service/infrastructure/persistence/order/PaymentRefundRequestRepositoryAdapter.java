package com.twohands.commerce_service.infrastructure.persistence.order;

import com.twohands.commerce_service.domain.order.PaymentRefundRequestRepository;
import com.twohands.commerce_service.domain.order.PaymentRefundRequestSummary;
import com.twohands.commerce_service.domain.payment.PaymentRefundRequestStatus;
import com.twohands.commerce_service.domain.payment.PaymentRefundRequestedBy;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PaymentRefundRequestRepositoryAdapter implements PaymentRefundRequestRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PaymentRefundRequestRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ActiveRefundRequestRow> findActiveByOrderId(UUID orderId) {
        String sql = """
                SELECT id, payment_id, order_id, requested_by::text AS requested_by, amount, created_at
                FROM payment_refund_requests
                WHERE order_id = :orderId AND status = 'REQUESTED'
                LIMIT 1
                """;
        List<ActiveRefundRequestRow> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("orderId", orderId),
                (rs, rowNum) -> new ActiveRefundRequestRow(
                        UUID.fromString(rs.getString("id")),
                        UUID.fromString(rs.getString("payment_id")),
                        UUID.fromString(rs.getString("order_id")),
                        PaymentRefundRequestedBy.valueOf(rs.getString("requested_by")),
                        rs.getBigDecimal("amount"),
                        rs.getTimestamp("created_at").toInstant()
                )
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public UUID createRequested(
            UUID paymentId,
            UUID orderId,
            PaymentRefundRequestedBy requestedBy,
            UUID requestedByUserId,
            BigDecimal amount,
            String reason,
            Instant now
    ) {
        UUID refundRequestId = UUID.randomUUID();
        String sql = """
                INSERT INTO payment_refund_requests(
                    id, payment_id, order_id, requested_by, requested_by_user_id,
                    status, amount, reason, created_at, updated_at
                ) VALUES (
                    :id, :paymentId, :orderId, CAST(:requestedBy AS payment_refund_requested_by),
                    :requestedByUserId, CAST(:status AS payment_refund_request_status),
                    :amount, :reason, :now, :now
                )
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", refundRequestId)
                .addValue("paymentId", paymentId)
                .addValue("orderId", orderId)
                .addValue("requestedBy", requestedBy.name())
                .addValue("requestedByUserId", requestedByUserId)
                .addValue("status", PaymentRefundRequestStatus.REQUESTED.name())
                .addValue("amount", amount)
                .addValue("reason", reason)
                .addValue("now", Timestamp.from(now)));
        return refundRequestId;
    }

    @Override
    public Optional<PaymentRefundRequestSummary> findSummaryByOrderId(UUID orderId) {
        String sql = """
                SELECT id, status::text AS status, requested_by::text AS requested_by, amount, created_at
                FROM payment_refund_requests
                WHERE order_id = :orderId AND status = 'REQUESTED'
                ORDER BY created_at DESC
                LIMIT 1
                """;
        List<PaymentRefundRequestSummary> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("orderId", orderId),
                (rs, rowNum) -> new PaymentRefundRequestSummary(
                        UUID.fromString(rs.getString("id")),
                        PaymentRefundRequestStatus.valueOf(rs.getString("status")),
                        PaymentRefundRequestedBy.valueOf(rs.getString("requested_by")),
                        rs.getBigDecimal("amount"),
                        rs.getTimestamp("created_at").toInstant()
                )
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }
}
