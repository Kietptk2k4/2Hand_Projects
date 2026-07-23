package com.twohands.commerce_service.infrastructure.persistence.admin;

import com.twohands.commerce_service.domain.admin.AdminShopDetailEntry;
import com.twohands.commerce_service.domain.admin.ViewShopDetailForModerationRepository;
import com.twohands.commerce_service.domain.shop.ShopStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ViewShopDetailForModerationRepositoryAdapter implements ViewShopDetailForModerationRepository {

    private static final String DETAIL_QUERY = """
            SELECT ss.id AS shop_id,
                   ss.seller_id,
                   ss.shop_name,
                   ss.description,
                   ss.avatar_url AS logo_url,
                   ss.status::text AS status,
                   ss.created_at,
                   ss.updated_at,
                   COALESCE(pc.total_count, 0) AS total_product_count,
                   COALESCE(pc.active_count, 0) AS active_product_count,
                   COALESCE(oc.open_count, 0) AS open_order_count
            FROM seller_shops ss
            LEFT JOIN (
                SELECT shop_id,
                       COUNT(*) AS total_count,
                       COUNT(*) FILTER (WHERE status = 'ACTIVE') AS active_count
                FROM products
                GROUP BY shop_id
            ) pc ON pc.shop_id = ss.id
            LEFT JOIN (
                SELECT p.shop_id, COUNT(DISTINCT o.id) AS open_count
                FROM orders o
                INNER JOIN order_items oi ON oi.order_id = o.id
                INNER JOIN products p ON p.id = oi.product_id
                WHERE o.status NOT IN ('COMPLETED', 'CANCELLED')
                GROUP BY p.shop_id
            ) oc ON oc.shop_id = ss.id
            WHERE ss.id = :shopId
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ViewShopDetailForModerationRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<AdminShopDetailEntry> findByShopId(UUID shopId) {
        MapSqlParameterSource params = new MapSqlParameterSource("shopId", shopId);
        return jdbcTemplate.query(DETAIL_QUERY, params, this::mapEntry).stream().findFirst();
    }

    private AdminShopDetailEntry mapEntry(ResultSet rs, int rowNum) throws SQLException {
        return new AdminShopDetailEntry(
                rs.getObject("shop_id", UUID.class),
                rs.getObject("seller_id", UUID.class),
                rs.getString("shop_name"),
                rs.getString("description"),
                rs.getString("logo_url"),
                ShopStatus.valueOf(rs.getString("status")),
                toInstant(rs.getTimestamp("created_at")),
                toInstant(rs.getTimestamp("updated_at")),
                rs.getLong("total_product_count"),
                rs.getLong("active_product_count"),
                rs.getLong("open_order_count")
        );
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}
