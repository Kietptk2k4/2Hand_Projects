package com.twohands.commerce_service.infrastructure.persistence.payment;

import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.payment.PaymentSupportListEntry;
import com.twohands.commerce_service.domain.payment.PaymentSupportPagedResult;
import com.twohands.commerce_service.domain.payment.PaymentSupportReconciliationStatus;
import com.twohands.commerce_service.domain.payment.PaymentSupportSearchCriteria;
import com.twohands.commerce_service.domain.payment.ViewPaymentsForSupportRepository;
import com.twohands.commerce_service.domain.support.WebhookSupportPageRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class ViewPaymentsForSupportRepositoryAdapter implements ViewPaymentsForSupportRepository {

    private static final String HAS_VALID_WEBHOOK = """
            EXISTS (
                SELECT 1
                FROM payment_webhook_logs w
                WHERE w.payos_order_code = p.payos_order_code
                  AND w.processed = true
                  AND w.signature_valid = true
            )
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ViewPaymentsForSupportRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public PaymentSupportPagedResult search(PaymentSupportSearchCriteria criteria, WebhookSupportPageRequest pageRequest) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String whereClause = buildWhereClause(criteria, params);

        long totalElements = count(whereClause, params);
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / pageRequest.size());

        params.addValue("limit", pageRequest.size());
        params.addValue("offset", (pageRequest.page() - 1) * pageRequest.size());

        String searchSql = """
                SELECT p.id AS payment_id,
                       p.order_id,
                       p.payment_method::text AS payment_method,
                       p.amount,
                       p.currency,
                       p.status::text AS payment_status,
                       p.paid_at,
                       p.created_at
                FROM payments p
                WHERE 1 = 1
                %s
                ORDER BY p.created_at DESC
                LIMIT :limit OFFSET :offset
                """.formatted(whereClause);

        List<PaymentSupportListEntry> items = jdbcTemplate.query(searchSql, params, this::mapRow);

        return new PaymentSupportPagedResult(
                items,
                pageRequest.page(),
                pageRequest.size(),
                totalElements,
                totalPages
        );
    }

    private long count(String whereClause, MapSqlParameterSource params) {
        String countSql = "SELECT COUNT(*) FROM payments p WHERE 1 = 1 " + whereClause;
        Long count = jdbcTemplate.queryForObject(countSql, params, Long.class);
        return count == null ? 0L : count;
    }

    private String buildWhereClause(PaymentSupportSearchCriteria criteria, MapSqlParameterSource params) {
        StringBuilder where = new StringBuilder();
        if (criteria.status() != null) {
            where.append(" AND p.status = :status::payment_status");
            params.addValue("status", criteria.status().name());
        }
        if (criteria.paymentMethod() != null) {
            where.append(" AND p.payment_method = :paymentMethod::payment_method");
            params.addValue("paymentMethod", criteria.paymentMethod().name());
        }
        if (criteria.orderId() != null) {
            where.append(" AND p.order_id = :orderId");
            params.addValue("orderId", criteria.orderId());
        }
        if (criteria.searchQuery() != null && !criteria.searchQuery().isBlank()) {
            where.append(" AND CAST(p.id AS TEXT) ILIKE :searchPattern");
            params.addValue("searchPattern", "%" + criteria.searchQuery() + "%");
        }
        if (criteria.reconciliationStatus() != null) {
            where.append(reconciliationClause(criteria.reconciliationStatus()));
        }
        if (criteria.from() != null) {
            where.append(" AND p.created_at >= :from");
            params.addValue("from", Timestamp.from(criteria.from()));
        }
        if (criteria.to() != null) {
            where.append(" AND p.created_at <= :to");
            params.addValue("to", Timestamp.from(criteria.to()));
        }
        return where.toString();
    }

    private String reconciliationClause(PaymentSupportReconciliationStatus reconciliationStatus) {
        return switch (reconciliationStatus) {
            case NOT_APPLICABLE -> " AND p.payment_method::text != 'PAYOS'";
            case RECONCILED -> " AND p.payment_method::text = 'PAYOS' AND p.status::text = 'PAID' AND " + HAS_VALID_WEBHOOK;
            case OUTSTANDING -> " AND p.payment_method::text = 'PAYOS' AND p.status::text = 'PAID' AND NOT (" + HAS_VALID_WEBHOOK + ")";
            case AWAITING_WEBHOOK -> " AND p.payment_method::text = 'PAYOS' AND p.status::text = 'PENDING' AND NOT (" + HAS_VALID_WEBHOOK + ")";
            case WEBHOOK_RECEIVED -> " AND p.payment_method::text = 'PAYOS' AND p.status::text = 'PENDING' AND " + HAS_VALID_WEBHOOK;
            case TERMINAL_RECONCILED -> " AND p.payment_method::text = 'PAYOS' AND p.status::text IN ('FAILED','CANCELLED','EXPIRED','REFUNDED') AND " + HAS_VALID_WEBHOOK;
            case TERMINAL_OUTSTANDING -> " AND p.payment_method::text = 'PAYOS' AND p.status::text IN ('FAILED','CANCELLED','EXPIRED','REFUNDED') AND NOT (" + HAS_VALID_WEBHOOK + ")";
        };
    }

    private PaymentSupportListEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new PaymentSupportListEntry(
                UUID.fromString(rs.getString("payment_id")),
                UUID.fromString(rs.getString("order_id")),
                PaymentMethod.valueOf(rs.getString("payment_method")),
                rs.getBigDecimal("amount"),
                rs.getString("currency"),
                PaymentStatus.valueOf(rs.getString("payment_status")),
                toInstant(rs.getTimestamp("paid_at")),
                rs.getTimestamp("created_at").toInstant()
        );
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}
