package com.twohands.commerce_service.infrastructure.persistence.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.application.payment.handlepaymentfailure.HandlePaymentFailureCommand;
import com.twohands.commerce_service.application.payment.handlepaymentfailure.HandlePaymentFailureUseCase;
import com.twohands.commerce_service.domain.order.ExpiredUnpaidOrderCandidate;
import com.twohands.commerce_service.domain.order.UnpaidOrderCancelOutcome;
import com.twohands.commerce_service.domain.order.UnpaidOrderCancellationRepository;
import com.twohands.commerce_service.domain.payment.HandlePaymentFailureResult;
import com.twohands.commerce_service.domain.payment.PaymentFailureOutcome;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class UnpaidOrderCancellationRepositoryAdapter implements UnpaidOrderCancellationRepository {

    private static final Logger log = LoggerFactory.getLogger(UnpaidOrderCancellationRepositoryAdapter.class);
    private static final String AUTO_CANCEL_REASON = "AUTO_CANCEL_UNPAID_ORDER";
    private static final String CHANGED_BY = "SYSTEM:AUTO_CANCEL_UNPAID_ORDER";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final HandlePaymentFailureUseCase handlePaymentFailureUseCase;
    private final ObjectMapper objectMapper;

    public UnpaidOrderCancellationRepositoryAdapter(
            NamedParameterJdbcTemplate jdbcTemplate,
            HandlePaymentFailureUseCase handlePaymentFailureUseCase,
            ObjectMapper objectMapper
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.handlePaymentFailureUseCase = handlePaymentFailureUseCase;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<ExpiredUnpaidOrderCandidate> findExpiredCandidates(
            int batchSize,
            Instant now,
            Instant orderCreatedBefore
    ) {
        String sql = """
                SELECT o.id AS order_id, p.id AS payment_id
                FROM orders o
                INNER JOIN payments p ON p.order_id = o.id
                WHERE o.status IN ('CREATED', 'AWAITING_PAYMENT')
                  AND o.payment_status = 'PENDING'
                  AND p.status = 'PENDING'
                  AND p.payment_method = 'PAYOS'
                  AND (
                    (p.expired_at IS NOT NULL AND p.expired_at < :now)
                    OR (p.checkout_url_expired_at IS NOT NULL AND p.checkout_url_expired_at < :now)
                    OR o.created_at < :orderCreatedBefore
                  )
                ORDER BY o.created_at ASC
                LIMIT :batchSize
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("now", Timestamp.from(now))
                .addValue("orderCreatedBefore", Timestamp.from(orderCreatedBefore))
                .addValue("batchSize", batchSize);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new ExpiredUnpaidOrderCandidate(
                UUID.fromString(rs.getString("order_id")),
                UUID.fromString(rs.getString("payment_id"))
        ));
    }

    @Override
    public boolean hasShipmentBlockingCancel(UUID orderId) {
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

    @Override
    @Transactional
    public UnpaidOrderCancelOutcome cancelExpiredUnpaidOrder(UUID orderId, UUID paymentId, Instant now) {
        HandlePaymentFailureResult result = handlePaymentFailureUseCase.execute(
                HandlePaymentFailureCommand.byPaymentId(
                        paymentId,
                        PaymentStatus.EXPIRED,
                        AUTO_CANCEL_REASON,
                        CHANGED_BY,
                        buildPaymentHistoryPayload(now)
                )
        );

        return mapOutcome(result);
    }

    private UnpaidOrderCancelOutcome mapOutcome(HandlePaymentFailureResult result) {
        return switch (result.outcome()) {
            case PROCESSED -> UnpaidOrderCancelOutcome.CANCELLED;
            case SKIPPED_ALREADY_PAID, SKIPPED_ALREADY_TERMINAL, SKIPPED_PAYMENT_NOT_PENDING ->
                    UnpaidOrderCancelOutcome.SKIPPED_ALREADY_TERMINAL;
            case SKIPPED_COD -> UnpaidOrderCancelOutcome.SKIPPED_COD;
            case SKIPPED_SHIPMENT_STARTED -> UnpaidOrderCancelOutcome.SKIPPED_SHIPMENT_STARTED;
            case SKIPPED_ORDER_NOT_CANCELLABLE -> UnpaidOrderCancelOutcome.SKIPPED_ALREADY_TERMINAL;
        };
    }

    private String buildPaymentHistoryPayload(Instant now) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("reason", AUTO_CANCEL_REASON);
        payload.put("processed_at", now.toString());
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            log.warn("Cannot serialize auto-cancel payment history payload", ex);
            return null;
        }
    }
}
