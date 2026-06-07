package com.twohands.commerce_service.infrastructure.persistence.shipment;

import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.shipment.SellerShipmentListEntry;
import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.domain.shipment.ViewSellerShipmentsRepository;
import com.twohands.commerce_service.domain.shipping.ShipmentType;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ViewSellerShipmentsRepositoryAdapter implements ViewSellerShipmentsRepository {

    private static final String BASE_FROM = """
            FROM shipments s
            LEFT JOIN shipping_address_snapshots sas ON sas.shipment_id = s.id
            WHERE s.seller_id = :sellerId
            """;

    private static final String SELECT_COLUMNS = """
            SELECT s.id AS shipment_id,
                   s.order_id,
                   s.carrier::text AS carrier,
                   s.shipment_type::text AS shipment_type,
                   s.status::text AS status,
                   s.tracking_number,
                   s.ghn_order_code,
                   sas.full_address AS delivery_address_summary,
                   s.created_at,
                   s.updated_at,
                   (
                       SELECT COUNT(*)
                       FROM order_items oi
                       WHERE oi.shipment_id = s.id
                   ) AS order_item_count
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ViewSellerShipmentsRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public long countBySellerId(UUID sellerId, Optional<ShipmentStatus> status, Optional<String> searchQuery) {
        MapSqlParameterSource params = baseParams(sellerId, status, searchQuery);
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) " + BASE_FROM + filterClause(status, searchQuery),
                params,
                Long.class
        );
        return count == null ? 0L : count;
    }

    @Override
    public List<SellerShipmentListEntry> findBySellerId(
            UUID sellerId,
            Optional<ShipmentStatus> status,
            Optional<String> searchQuery,
            PageQuery pageQuery
    ) {
        MapSqlParameterSource params = baseParams(sellerId, status, searchQuery)
                .addValue("limit", pageQuery.limit())
                .addValue("offset", pageQuery.offset());

        return jdbcTemplate.query(
                SELECT_COLUMNS + BASE_FROM + filterClause(status, searchQuery) + """
                        ORDER BY s.updated_at DESC, s.created_at DESC
                        LIMIT :limit OFFSET :offset
                        """,
                params,
                this::mapEntry
        );
    }

    @Override
    public Map<String, Long> countByStatusForSeller(UUID sellerId) {
        String sql = """
                SELECT status::text AS status, COUNT(*) AS total
                FROM shipments
                WHERE seller_id = :sellerId
                GROUP BY status
                """;
        Map<String, Long> counts = new HashMap<>();
        jdbcTemplate.query(sql, Map.of("sellerId", sellerId), (rs, rowNum) -> {
            counts.put(rs.getString("status"), rs.getLong("total"));
            return null;
        });
        return Map.copyOf(counts);
    }

    private MapSqlParameterSource baseParams(
            UUID sellerId,
            Optional<ShipmentStatus> status,
            Optional<String> searchQuery
    ) {
        MapSqlParameterSource params = new MapSqlParameterSource("sellerId", sellerId);
        status.ifPresent(value -> params.addValue("status", value.name()));
        searchQuery.ifPresent(value -> params.addValue("searchPattern", "%" + value + "%"));
        return params;
    }

    private String filterClause(Optional<ShipmentStatus> status, Optional<String> searchQuery) {
        StringBuilder clause = new StringBuilder();
        status.ifPresent(ignored -> clause.append(" AND s.status::text = :status"));
        searchQuery.ifPresent(ignored -> clause.append("""
                 AND (
                    CAST(s.id AS TEXT) ILIKE :searchPattern
                    OR CAST(s.order_id AS TEXT) ILIKE :searchPattern
                    OR COALESCE(s.tracking_number, '') ILIKE :searchPattern
                    OR COALESCE(s.ghn_order_code, '') ILIKE :searchPattern
                )
                """));
        return clause.toString();
    }

    private SellerShipmentListEntry mapEntry(ResultSet rs, int rowNum) throws SQLException {
        return new SellerShipmentListEntry(
                rs.getObject("shipment_id", UUID.class),
                rs.getObject("order_id", UUID.class),
                ShipmentCarrier.valueOf(rs.getString("carrier")),
                ShipmentType.valueOf(rs.getString("shipment_type")),
                ShipmentStatus.valueOf(rs.getString("status")),
                rs.getString("tracking_number"),
                rs.getString("ghn_order_code"),
                rs.getString("delivery_address_summary"),
                toInstant(rs.getTimestamp("created_at")),
                toInstant(rs.getTimestamp("updated_at")),
                rs.getInt("order_item_count")
        );
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}
