package com.twohands.commerce_service.infrastructure.persistence.admin;

import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.admin.AdminShopListEntry;
import com.twohands.commerce_service.domain.admin.AdminShopListSort;
import com.twohands.commerce_service.domain.admin.ViewAdminShopsForModerationRepository;
import com.twohands.commerce_service.domain.shop.ShopStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ViewAdminShopsForModerationRepositoryAdapter implements ViewAdminShopsForModerationRepository {

    private static final String BASE_FROM = """
            FROM seller_shops ss
            WHERE 1 = 1
            """;

    private static final String SELECT_COLUMNS = """
            SELECT ss.id AS shop_id,
                   ss.seller_id,
                   ss.shop_name,
                   ss.avatar_url AS logo_url,
                   ss.status::text AS status,
                   ss.created_at
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ViewAdminShopsForModerationRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public long count(Optional<ShopStatus> status, Optional<String> searchQuery) {
        MapSqlParameterSource params = baseParams(status, searchQuery);
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) " + BASE_FROM + filterClause(status, searchQuery),
                params,
                Long.class
        );
        return count == null ? 0L : count;
    }

    @Override
    public List<AdminShopListEntry> find(
            Optional<ShopStatus> status,
            Optional<String> searchQuery,
            AdminShopListSort sort,
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

    private MapSqlParameterSource baseParams(Optional<ShopStatus> status, Optional<String> searchQuery) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        status.ifPresent(value -> params.addValue("status", value.name()));
        searchQuery.ifPresent(value -> params.addValue("searchPattern", "%" + value + "%"));
        return params;
    }

    private String filterClause(Optional<ShopStatus> status, Optional<String> searchQuery) {
        StringBuilder clause = new StringBuilder();
        status.ifPresent(ignored -> clause.append(" AND ss.status::text = :status"));
        searchQuery.ifPresent(ignored -> clause.append("""
                 AND (
                    ss.shop_name ILIKE :searchPattern
                    OR CAST(ss.id AS TEXT) ILIKE :searchPattern
                    OR CAST(ss.seller_id AS TEXT) ILIKE :searchPattern
                )
                """));
        return clause.toString();
    }

    private String orderByClause(AdminShopListSort sort) {
        return switch (sort) {
            case OLDEST -> " ORDER BY ss.created_at ASC, ss.id ASC ";
            case NAME_ASC -> " ORDER BY ss.shop_name ASC, ss.id ASC ";
            case NEWEST -> " ORDER BY ss.created_at DESC, ss.id DESC ";
        };
    }

    private AdminShopListEntry mapEntry(ResultSet rs, int rowNum) throws SQLException {
        return new AdminShopListEntry(
                rs.getObject("shop_id", UUID.class),
                rs.getObject("seller_id", UUID.class),
                rs.getString("shop_name"),
                rs.getString("logo_url"),
                ShopStatus.valueOf(rs.getString("status")),
                toInstant(rs.getTimestamp("created_at"))
        );
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}
