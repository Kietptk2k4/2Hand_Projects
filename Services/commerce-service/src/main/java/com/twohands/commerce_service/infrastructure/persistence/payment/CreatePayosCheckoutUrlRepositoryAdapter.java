package com.twohands.commerce_service.infrastructure.persistence.payment;

import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.payment.CreatePayosCheckoutUrlRepository;
import com.twohands.commerce_service.domain.payment.CreatePayosCheckoutUrlResult;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentPayosSnapshot;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.payment.PayosPaymentLinkResult;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CreatePayosCheckoutUrlRepositoryAdapter implements CreatePayosCheckoutUrlRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public CreatePayosCheckoutUrlRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<PaymentPayosSnapshot> findPaymentForBuyer(UUID paymentId, UUID buyerId) {
        String sql = """
                SELECT p.id AS payment_id,
                       p.order_id,
                       o.buyer_id,
                       p.amount,
                       p.payment_method,
                       p.status AS payment_status,
                       o.status AS order_status,
                       p.payos_order_code,
                       p.payos_checkout_url,
                       p.checkout_url_expired_at,
                       p.expired_at AS payment_expired_at
                FROM payments p
                INNER JOIN orders o ON o.id = p.order_id
                WHERE p.id = :paymentId
                  AND o.buyer_id = :buyerId
                """;
        List<PaymentPayosSnapshot> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("paymentId", paymentId)
                        .addValue("buyerId", buyerId),
                (rs, rowNum) -> mapSnapshot(rs)
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    @Transactional
    public CreatePayosCheckoutUrlResult savePayosCheckoutFields(
            UUID paymentId,
            UUID orderId,
            PayosPaymentLinkResult providerResult,
            Instant occurredAt
    ) {
        String sql = """
                UPDATE payments
                SET payos_order_code = :payosOrderCode,
                    payos_checkout_url = :payosCheckoutUrl,
                    checkout_url_expired_at = :checkoutUrlExpiredAt,
                    provider_response = CAST(:providerResponse AS jsonb),
                    updated_at = :now
                WHERE id = :paymentId
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("paymentId", paymentId)
                .addValue("payosOrderCode", providerResult.payosOrderCode())
                .addValue("payosCheckoutUrl", providerResult.payosCheckoutUrl())
                .addValue("checkoutUrlExpiredAt", providerResult.checkoutUrlExpiredAt() == null
                        ? null
                        : Timestamp.from(providerResult.checkoutUrlExpiredAt()))
                .addValue("providerResponse", providerResult.providerResponseJson())
                .addValue("now", Timestamp.from(occurredAt)));

        return new CreatePayosCheckoutUrlResult(
                paymentId,
                orderId,
                providerResult.payosOrderCode(),
                providerResult.payosCheckoutUrl(),
                providerResult.checkoutUrlExpiredAt(),
                false
        );
    }

    private PaymentPayosSnapshot mapSnapshot(ResultSet rs) throws SQLException {
        return new PaymentPayosSnapshot(
                UUID.fromString(rs.getString("payment_id")),
                UUID.fromString(rs.getString("order_id")),
                UUID.fromString(rs.getString("buyer_id")),
                rs.getBigDecimal("amount"),
                PaymentMethod.valueOf(rs.getString("payment_method")),
                PaymentStatus.valueOf(rs.getString("payment_status")),
                OrderStatus.valueOf(rs.getString("order_status")),
                rs.getString("payos_order_code"),
                rs.getString("payos_checkout_url"),
                toInstant(rs.getTimestamp("checkout_url_expired_at")),
                toInstant(rs.getTimestamp("payment_expired_at"))
        );
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}
