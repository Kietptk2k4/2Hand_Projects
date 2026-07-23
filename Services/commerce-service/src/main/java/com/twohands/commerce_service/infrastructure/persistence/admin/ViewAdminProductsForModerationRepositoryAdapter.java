package com.twohands.commerce_service.infrastructure.persistence.admin;

import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.admin.AdminProductListEntry;
import com.twohands.commerce_service.domain.admin.AdminProductListSort;
import com.twohands.commerce_service.domain.admin.ViewAdminProductsForModerationRepository;
import com.twohands.commerce_service.domain.product.ProductStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ViewAdminProductsForModerationRepositoryAdapter implements ViewAdminProductsForModerationRepository {

    private static final String BASE_FROM = """
            FROM products p
            INNER JOIN seller_shops ss ON ss.id = p.shop_id
            INNER JOIN product_categories pc ON pc.id = p.category_id
            LEFT JOIN LATERAL (
                SELECT pp.price,
                       COALESCE(pp.sale_price, pp.price) AS effective_price
                FROM product_prices pp
                WHERE pp.product_id = p.id
                  AND pp.start_at <= :now
                  AND (pp.end_at IS NULL OR pp.end_at > :now)
                ORDER BY pp.start_at DESC
                LIMIT 1
            ) active_price ON TRUE
            LEFT JOIN LATERAL (
                SELECT pm.media_url
                FROM product_media pm
                WHERE pm.product_id = p.id
                ORDER BY pm.sort_order ASC, pm.created_at ASC
                LIMIT 1
            ) thumbnail ON TRUE
            WHERE 1 = 1
            """;

    private static final String SELECT_COLUMNS = """
            SELECT p.id AS product_id,
                   p.seller_id,
                   p.shop_id,
                   ss.shop_name,
                   p.title,
                   thumbnail.media_url AS thumbnail_url,
                   p.category_id,
                   pc.name AS category_name,
                   active_price.price,
                   active_price.effective_price,
                   p.status::text AS status,
                   p.created_at,
                   CASE WHEN p.status::text = 'REMOVED' THEN p.updated_at ELSE NULL END AS removed_at,
                   p.remove_reason
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final Clock clock;

    public ViewAdminProductsForModerationRepositoryAdapter(
            NamedParameterJdbcTemplate jdbcTemplate,
            Clock clock
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.clock = clock;
    }

    @Override
    public long count(Optional<ProductStatus> status, Optional<String> searchQuery) {
        MapSqlParameterSource params = baseParams(status, searchQuery);
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) " + BASE_FROM + filterClause(status, searchQuery),
                params,
                Long.class
        );
        return count == null ? 0L : count;
    }

    @Override
    public List<AdminProductListEntry> find(
            Optional<ProductStatus> status,
            Optional<String> searchQuery,
            AdminProductListSort sort,
            PageQuery pageQuery
    ) {
        MapSqlParameterSource params = baseParams(status, searchQuery)
                .addValue("limit", pageQuery.limit())
                .addValue("offset", pageQuery.offset());

        return jdbcTemplate.query(
                SELECT_COLUMNS + BASE_FROM + filterClause(status, searchQuery) + orderByClause(sort) + """
                        LIMIT :limit OFFSET :offset
                        """,
                params,
                this::mapEntry
        );
    }

    private String orderByClause(AdminProductListSort sort) {
        return switch (sort) {
            case OLDEST -> " ORDER BY p.created_at ASC, p.id ASC ";
            case PRICE_ASC -> " ORDER BY active_price.effective_price ASC NULLS LAST, p.id DESC ";
            case PRICE_DESC -> " ORDER BY active_price.effective_price DESC NULLS LAST, p.id DESC ";
            case UPDATED_AT -> " ORDER BY p.updated_at DESC, p.id DESC ";
            case NEWEST -> " ORDER BY p.created_at DESC, p.id DESC ";
        };
    }

    private MapSqlParameterSource baseParams(Optional<ProductStatus> status, Optional<String> searchQuery) {
        MapSqlParameterSource params = new MapSqlParameterSource("now", Timestamp.from(clock.instant()));
        status.ifPresent(value -> params.addValue("status", value.name()));
        searchQuery.ifPresent(value -> params.addValue("searchPattern", "%" + value + "%"));
        return params;
    }

    private String filterClause(Optional<ProductStatus> status, Optional<String> searchQuery) {
        StringBuilder clause = new StringBuilder();
        status.ifPresent(ignored -> clause.append(" AND p.status::text = :status"));
        searchQuery.ifPresent(ignored -> clause.append("""
                 AND (
                    p.title ILIKE :searchPattern
                    OR CAST(p.id AS TEXT) ILIKE :searchPattern
                    OR CAST(p.shop_id AS TEXT) ILIKE :searchPattern
                    OR ss.shop_name ILIKE :searchPattern
                )
                """));
        return clause.toString();
    }

    private AdminProductListEntry mapEntry(ResultSet rs, int rowNum) throws SQLException {
        ProductStatus productStatus = ProductStatus.valueOf(rs.getString("status"));
        return new AdminProductListEntry(
                rs.getObject("product_id", UUID.class),
                rs.getObject("seller_id", UUID.class),
                rs.getObject("shop_id", UUID.class),
                rs.getString("shop_name"),
                rs.getString("title"),
                rs.getString("thumbnail_url"),
                rs.getObject("category_id", UUID.class),
                rs.getString("category_name"),
                rs.getBigDecimal("price"),
                rs.getBigDecimal("effective_price"),
                productStatus,
                toInstant(rs.getTimestamp("created_at")),
                toInstant(rs.getTimestamp("removed_at")),
                rs.getString("remove_reason")
        );
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}
