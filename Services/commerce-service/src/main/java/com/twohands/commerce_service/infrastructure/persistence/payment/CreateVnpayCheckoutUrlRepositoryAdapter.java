package com.twohands.commerce_service.infrastructure.persistence.payment;

import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.payment.CreateVnpayCheckoutUrlRepository;
import com.twohands.commerce_service.domain.payment.CreateVnpayCheckoutUrlResult;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.payment.PaymentVnpaySnapshot;
import com.twohands.commerce_service.domain.payment.VnpayPaymentUrlResult;
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
public class CreateVnpayCheckoutUrlRepositoryAdapter implements CreateVnpayCheckoutUrlRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public CreateVnpayCheckoutUrlRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<PaymentVnpaySnapshot> findPaymentForBuyer(UUID paymentId, UUID buyerId) {
        return querySnapshot("""
                SELECT p.id AS payment_id,
                       p.order_id,
                       o.buyer_id,
                       p.amount,
                       p.payment_method,
                       p.status AS payment_status,
                       o.status AS order_status,
                       p.vnpay_txn_ref,
                       p.expired_at AS payment_expired_at
                FROM payments p
                INNER JOIN orders o ON o.id = p.order_id
                WHERE p.id = :paymentId
                  AND o.buyer_id = :buyerId
                """, new MapSqlParameterSource()
                .addValue("paymentId", paymentId)
                .addValue("buyerId", buyerId));
    }

    @Override
    public Optional<PaymentVnpaySnapshot> findPaymentByOrderForBuyer(UUID orderId, UUID buyerId) {
        return querySnapshot("""
                SELECT p.id AS payment_id,
                       p.order_id,
                       o.buyer_id,
                       p.amount,
                       p.payment_method,
                       p.status AS payment_status,
                       o.status AS order_status,
                       p.vnpay_txn_ref,
                       p.expired_at AS payment_expired_at
                FROM payments p
                INNER JOIN orders o ON o.id = p.order_id
                WHERE p.order_id = :orderId
                  AND o.buyer_id = :buyerId
                """, new MapSqlParameterSource()
                .addValue("orderId", orderId)
                .addValue("buyerId", buyerId));
    }

    @Override
    @Transactional
    public CreateVnpayCheckoutUrlResult saveVnpayCheckoutFields(
            UUID paymentId,
            UUID orderId,
            VnpayPaymentUrlResult providerResult,
            Instant occurredAt
    ) {
        String sql = """
                UPDATE payments
                SET vnpay_txn_ref = :txnRef,
                    provider_response = CAST(:providerResponse AS jsonb),
                    updated_at = :now
                WHERE id = :paymentId
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("paymentId", paymentId)
                .addValue("txnRef", providerResult.txnRef())
                .addValue("providerResponse", providerResult.providerResponseJson())
                .addValue("now", Timestamp.from(occurredAt)));

        return new CreateVnpayCheckoutUrlResult(
                paymentId,
                orderId,
                providerResult.txnRef(),
                providerResult.paymentUrl()
        );
    }

    private Optional<PaymentVnpaySnapshot> querySnapshot(String sql, MapSqlParameterSource params) {
        List<PaymentVnpaySnapshot> rows = jdbcTemplate.query(sql, params, this::mapSnapshot);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    private PaymentVnpaySnapshot mapSnapshot(ResultSet rs, int rowNum) throws SQLException {
        return new PaymentVnpaySnapshot(
                UUID.fromString(rs.getString("payment_id")),
                UUID.fromString(rs.getString("order_id")),
                UUID.fromString(rs.getString("buyer_id")),
                rs.getBigDecimal("amount"),
                PaymentMethod.valueOf(rs.getString("payment_method")),
                PaymentStatus.valueOf(rs.getString("payment_status")),
                OrderStatus.valueOf(rs.getString("order_status")),
                rs.getString("vnpay_txn_ref"),
                toInstant(rs.getTimestamp("payment_expired_at"))
        );
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}
