package com.twohands.commerce_service.infrastructure.persistence.payment;

import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.payment.ViewPaymentStatusRepository;
import com.twohands.commerce_service.domain.payment.ViewPaymentStatusSnapshot;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ViewPaymentStatusRepositoryAdapter implements ViewPaymentStatusRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ViewPaymentStatusRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ViewPaymentStatusSnapshot> findByPaymentIdAndBuyerId(UUID paymentId, UUID buyerId) {
        String sql = """
                SELECT p.id AS payment_id,
                       p.order_id,
                       p.payment_method::text AS payment_method,
                       p.amount,
                       p.currency,
                       p.status::text AS payment_status,
                       p.paid_at,
                       p.expired_at,
                       p.payos_checkout_url,
                       p.checkout_url_expired_at,
                       o.status::text AS order_status,
                       o.payment_status::text AS order_payment_status
                FROM payments p
                INNER JOIN orders o ON o.id = p.order_id
                WHERE p.id = :paymentId
                  AND o.buyer_id = :buyerId
                """;
        List<ViewPaymentStatusSnapshot> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("paymentId", paymentId)
                        .addValue("buyerId", buyerId),
                (rs, rowNum) -> mapSnapshot(rs)
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    private ViewPaymentStatusSnapshot mapSnapshot(ResultSet rs) throws SQLException {
        return new ViewPaymentStatusSnapshot(
                UUID.fromString(rs.getString("payment_id")),
                UUID.fromString(rs.getString("order_id")),
                PaymentMethod.valueOf(rs.getString("payment_method")),
                rs.getBigDecimal("amount"),
                rs.getString("currency"),
                PaymentStatus.valueOf(rs.getString("payment_status")),
                optionalInstant(rs.getTimestamp("paid_at")),
                optionalInstant(rs.getTimestamp("expired_at")),
                rs.getString("payos_checkout_url"),
                optionalInstant(rs.getTimestamp("checkout_url_expired_at")),
                OrderStatus.valueOf(rs.getString("order_status")),
                PaymentStatus.valueOf(rs.getString("order_payment_status"))
        );
    }

    private Instant optionalInstant(Timestamp value) {
        return value == null ? null : value.toInstant();
    }
}
