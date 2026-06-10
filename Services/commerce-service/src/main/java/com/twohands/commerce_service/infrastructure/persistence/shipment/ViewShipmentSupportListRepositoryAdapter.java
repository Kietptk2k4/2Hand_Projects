package com.twohands.commerce_service.infrastructure.persistence.shipment;

import com.twohands.commerce_service.domain.shipment.ShipmentSupportListEntry;
import com.twohands.commerce_service.domain.shipment.ShipmentSupportListPagedResult;
import com.twohands.commerce_service.domain.shipment.ShipmentSupportListSearchCriteria;
import com.twohands.commerce_service.domain.shipment.ShipmentSupportListSortField;
import com.twohands.commerce_service.domain.shipment.ViewShipmentSupportListRepository;
import com.twohands.commerce_service.domain.support.WebhookSupportPageRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class ViewShipmentSupportListRepositoryAdapter implements ViewShipmentSupportListRepository {

    private static final String BASE_FROM = """
            FROM shipments s
            WHERE 1 = 1
            """;

    private static final String SELECT_COLUMNS = """
            SELECT s.id AS shipment_id,
                   s.order_id,
                   s.seller_id,
                   s.carrier::text AS carrier,
                   s.status::text AS internal_status,
                   s.tracking_number,
                   s.ghn_order_code,
                   s.shipped_at,
                   s.created_at,
                   s.updated_at
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ViewShipmentSupportListRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public long count(ShipmentSupportListSearchCriteria criteria) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) " + BASE_FROM + filterClause(criteria),
                toParams(criteria),
                Long.class
        );
        return count == null ? 0L : count;
    }

    @Override
    public ShipmentSupportListPagedResult search(
            ShipmentSupportListSearchCriteria criteria,
            WebhookSupportPageRequest pageRequest
    ) {
        long totalElements = count(criteria);
        int totalPages = totalElements == 0
                ? 0
                : (int) Math.ceil((double) totalElements / pageRequest.size());

        if (totalElements == 0) {
            return new ShipmentSupportListPagedResult(
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

        List<ShipmentSupportListEntry> items = jdbcTemplate.query(
                SELECT_COLUMNS + BASE_FROM + filterClause(criteria) + orderByClause(criteria.sortField()) + """
                        LIMIT :limit OFFSET :offset
                        """,
                params,
                this::mapEntry
        );

        return new ShipmentSupportListPagedResult(
                items,
                pageRequest.page(),
                pageRequest.size(),
                totalElements,
                totalPages
        );
    }

    private MapSqlParameterSource toParams(ShipmentSupportListSearchCriteria criteria) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        criteria.status().ifPresent(status -> params.addValue("status", status.name()));
        criteria.carrier().ifPresent(carrier -> params.addValue("carrier", carrier.name()));
        return params;
    }

    private String filterClause(ShipmentSupportListSearchCriteria criteria) {
        StringBuilder clause = new StringBuilder();
        if (criteria.status().isPresent()) {
            clause.append(" AND s.status::text = :status");
        }
        if (criteria.carrier().isPresent()) {
            clause.append(" AND s.carrier::text = :carrier");
        }
        return clause.toString();
    }

    private String orderByClause(ShipmentSupportListSortField sortField) {
        return switch (sortField) {
            case CREATED_AT -> " ORDER BY s.created_at DESC, s.id DESC ";
            case SHIPPED_AT -> " ORDER BY s.shipped_at DESC NULLS LAST, s.created_at DESC, s.id DESC ";
            case UPDATED_AT -> " ORDER BY s.updated_at DESC, s.id DESC ";
        };
    }

    private ShipmentSupportListEntry mapEntry(ResultSet rs, int rowNum) throws SQLException {
        return new ShipmentSupportListEntry(
                rs.getObject("shipment_id", UUID.class),
                rs.getObject("order_id", UUID.class),
                rs.getObject("seller_id", UUID.class),
                rs.getString("carrier"),
                rs.getString("internal_status"),
                rs.getString("tracking_number"),
                rs.getString("ghn_order_code"),
                toInstant(rs.getTimestamp("shipped_at")),
                toInstant(rs.getTimestamp("created_at")),
                toInstant(rs.getTimestamp("updated_at"))
        );
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}
