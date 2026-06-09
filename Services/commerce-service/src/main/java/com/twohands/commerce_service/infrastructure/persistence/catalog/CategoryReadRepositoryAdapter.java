package com.twohands.commerce_service.infrastructure.persistence.catalog;

import com.twohands.commerce_service.domain.catalog.ActiveCategory;
import com.twohands.commerce_service.domain.catalog.CategoryReadRepository;
import com.twohands.commerce_service.domain.catalog.CategorySummary;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CategoryReadRepositoryAdapter implements CategoryReadRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public CategoryReadRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ActiveCategory> findById(UUID categoryId) {
        String sql = """
                SELECT id, name, slug, path
                FROM product_categories
                WHERE id = :categoryId
                  AND is_active = TRUE
                """;
        List<ActiveCategory> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("categoryId", categoryId),
                (rs, rowNum) -> new ActiveCategory(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("name"),
                        rs.getString("slug"),
                        rs.getString("path")
                )
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public List<UUID> resolveCategoryIdsForFilter(UUID categoryId, String categoryPath, boolean includeChildren) {
        if (!includeChildren) {
            return List.of(categoryId);
        }

        String sql = """
                SELECT id
                FROM product_categories
                WHERE is_active = TRUE
                  AND (id = :categoryId OR path LIKE :pathPrefix)
                """;
        ActiveCategory category = new ActiveCategory(categoryId, null, null, categoryPath);
        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("categoryId", categoryId)
                        .addValue("pathPrefix", category.subtreePathPrefix()),
                (rs, rowNum) -> UUID.fromString(rs.getString("id"))
        );
    }

    @Override
    public List<CategorySummary> findActiveSummaries(
            Integer minLevel,
            Integer maxLevel,
            boolean leafOnly,
            boolean includeProductCounts,
            Instant now
    ) {
        StringBuilder sql = new StringBuilder("""
                SELECT pc.id,
                       pc.name,
                       pc.slug,
                       pc.parent_id,
                       pc.level,
                       pc.path,
                       NOT EXISTS (
                           SELECT 1
                           FROM product_categories child
                           WHERE child.parent_id = pc.id
                             AND child.is_active = TRUE
                       ) AS is_leaf
                """);

        if (includeProductCounts) {
            sql.append("""
                    ,
                           COALESCE((
                               SELECT COUNT(DISTINCT p.id)
                               FROM products p
                               INNER JOIN seller_shops s ON s.id = p.shop_id AND s.status = 'ACTIVE'
                               INNER JOIN product_categories desc_cat
                                   ON desc_cat.id = p.category_id
                                  AND desc_cat.is_active = TRUE
                                  AND desc_cat.path LIKE pc.path || '%'
                               INNER JOIN LATERAL (
                                   SELECT price, sale_price
                                   FROM product_prices pp
                                   WHERE pp.product_id = p.id
                                     AND pp.start_at <= :now
                                     AND (pp.end_at IS NULL OR pp.end_at > :now)
                                   ORDER BY pp.start_at DESC
                                   LIMIT 1
                               ) active_price ON TRUE
                               WHERE p.status IN ('ACTIVE', 'OUT_OF_STOCK')
                           ), 0) AS product_count
                    """);
        } else {
            sql.append(",\n       0 AS product_count\n");
        }

        sql.append("""
                FROM product_categories pc
                WHERE pc.is_active = TRUE
                """);

        MapSqlParameterSource params = new MapSqlParameterSource("now", Timestamp.from(now));
        List<String> filters = new ArrayList<>();

        if (minLevel != null) {
            filters.add("pc.level >= :minLevel");
            params.addValue("minLevel", minLevel);
        }
        if (maxLevel != null) {
            filters.add("pc.level <= :maxLevel");
            params.addValue("maxLevel", maxLevel);
        }
        if (leafOnly) {
            filters.add("""
                    NOT EXISTS (
                        SELECT 1
                        FROM product_categories child
                        WHERE child.parent_id = pc.id
                          AND child.is_active = TRUE
                    )
                    """);
        }

        if (!filters.isEmpty()) {
            sql.append(" AND ").append(String.join(" AND ", filters));
        }

        sql.append(" ORDER BY pc.level ASC, pc.path ASC");

        return jdbcTemplate.query(sql.toString(), params, (rs, rowNum) -> mapCategorySummary(rs));
    }

    private CategorySummary mapCategorySummary(ResultSet rs) throws SQLException {
        String parentId = rs.getString("parent_id");
        return new CategorySummary(
                UUID.fromString(rs.getString("id")),
                rs.getString("name"),
                rs.getString("slug"),
                parentId == null ? null : UUID.fromString(parentId),
                rs.getInt("level"),
                rs.getBoolean("is_leaf"),
                rs.getLong("product_count")
        );
    }
}
