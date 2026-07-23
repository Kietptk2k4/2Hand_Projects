package com.twohands.commerce_service.infrastructure.persistence.order;

import com.twohands.commerce_service.domain.order.OrderSupportListEntry;
import com.twohands.commerce_service.domain.order.OrderSupportListPagedResult;
import com.twohands.commerce_service.domain.order.OrderSupportListSearchCriteria;
import com.twohands.commerce_service.domain.order.OrderSupportListSortField;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.order.ViewOrdersForSupportRepository;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
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
public class ViewOrdersForSupportRepositoryAdapter implements ViewOrdersForSupportRepository {

    private static final String BASE_FROM = """
            FROM orders o
            WHERE 1 = 1
            """;

    private static final String SELECT_COLUMNS = """
            SELECT o.id AS order_id,
                   o.buyer_id,
                   o.status::text AS order_status,
                   o.payment_status::text AS payment_status,
                   o.payment_method::text AS payment_method,
                   o.final_amount,
                   o.created_at,
                   o.updated_at
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ViewOrdersForSupportRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public OrderSupportListPagedResult search(
            OrderSupportListSearchCriteria criteria,
            WebhookSupportPageRequest pageRequest
    ) {
        long totalElements = count(criteria);
        int totalPages = totalElements == 0
                ? 0
                : (int) Math.ceil((double) totalElements / pageRequest.size());

        if (totalElements == 0) {
            return new OrderSupportListPagedResult(
                    List.of(),
                    pageRequest.page(),
                    pageRequest.size(),
                    0L,
                    totalPages
            );
        }

        MapSqlParameterSource params = toParams(criteria)
                .addValue("limit", pageRequest.size())
                .addValue("offset", (pageRequest.page() - 1) * pageRequest.size());

        List<OrderSupportListEntry> items = jdbcTemplate.query(
                SELECT_COLUMNS + BASE_FROM + filterClause(criteria) + orderByClause(criteria.sortField()) + """
                        LIMIT :limit OFFSET :offset
                        """,
                params,
                this::mapEntry
        );

        return new OrderSupportListPagedResult(
                items,
                pageRequest.page(),
                pageRequest.size(),
                totalElements,
                totalPages
        );
    }

    private long count(OrderSupportListSearchCriteria criteria) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) " + BASE_FROM + filterClause(criteria),
                toParams(criteria),
                Long.class
        );
        return count == null ? 0L : count;
    }

    private MapSqlParameterSource toParams(OrderSupportListSearchCriteria criteria) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        criteria.status().ifPresent(status -> params.addValue("status", status.name()));
        criteria.paymentMethod().ifPresent(method -> params.addValue("paymentMethod", method.name()));
        criteria.paymentStatus().ifPresent(status -> params.addValue("paymentStatus", status.name()));
        criteria.searchQuery().ifPresent(value -> params.addValue("searchPattern", "%" + value + "%"));
        if (criteria.from() != null) {
            params.addValue("from", Timestamp.from(criteria.from()));
        }
        if (criteria.to() != null) {
            params.addValue("to", Timestamp.from(criteria.to()));
        }
        return params;
    }

    private String filterClause(OrderSupportListSearchCriteria criteria) {
        StringBuilder clause = new StringBuilder();
        if (criteria.status().isPresent()) {
            clause.append(" AND o.status::text = :status");
        }
        if (criteria.paymentMethod().isPresent()) {
            clause.append(" AND o.payment_method::text = :paymentMethod");
        }
        if (criteria.paymentStatus().isPresent()) {
            clause.append(" AND o.payment_status::text = :paymentStatus");
        }
        if (criteria.searchQuery().isPresent()) {
            clause.append("""
                     AND (
                        CAST(o.id AS TEXT) ILIKE :searchPattern
                        OR CAST(o.buyer_id AS TEXT) ILIKE :searchPattern
                    )
                    """);
        }
        if (criteria.from() != null) {
            clause.append(" AND o.created_at >= :from");
        }
        if (criteria.to() != null) {
            clause.append(" AND o.created_at <= :to");
        }
        return clause.toString();
    }

    private String orderByClause(OrderSupportListSortField sortField) {
        return switch (sortField) {
            case CREATED_AT -> " ORDER BY o.created_at DESC, o.id DESC ";
            case UPDATED_AT -> " ORDER BY o.updated_at DESC, o.id DESC ";
        };
    }

    private OrderSupportListEntry mapEntry(ResultSet rs, int rowNum) throws SQLException {
        return new OrderSupportListEntry(
                rs.getObject("order_id", UUID.class),
                rs.getObject("buyer_id", UUID.class),
                OrderStatus.valueOf(rs.getString("order_status")),
                PaymentStatus.valueOf(rs.getString("payment_status")),
                PaymentMethod.valueOf(rs.getString("payment_method")),
                rs.getBigDecimal("final_amount"),
                toInstant(rs.getTimestamp("created_at")),
                toInstant(rs.getTimestamp("updated_at"))
        );
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}
