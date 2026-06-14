package com.twohands.commerce_service.infrastructure.persistence.catalog;

import com.twohands.commerce_service.domain.catalog.admin.AdminBrandRow;
import com.twohands.commerce_service.domain.catalog.admin.AdminCategoryRow;
import com.twohands.commerce_service.domain.catalog.admin.CatalogAdminRepository;
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
public class CatalogAdminRepositoryAdapter implements CatalogAdminRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public CatalogAdminRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<AdminCategoryRow> findAllCategories(Boolean activeOnly, String query) {
        StringBuilder sql = new StringBuilder("""
                SELECT pc.id,
                       pc.name,
                       pc.slug,
                       pc.parent_id,
                       pc.level,
                       pc.path,
                       pc.is_active,
                       pc.created_at,
                       pc.updated_at,
                       COALESCE((
                           SELECT COUNT(*)
                           FROM products p
                           WHERE p.category_id = pc.id
                       ), 0) AS product_count
                FROM product_categories pc
                WHERE 1 = 1
                """);

        MapSqlParameterSource params = new MapSqlParameterSource();
        if (Boolean.TRUE.equals(activeOnly)) {
            sql.append(" AND pc.is_active = TRUE");
        } else if (Boolean.FALSE.equals(activeOnly)) {
            sql.append(" AND pc.is_active = FALSE");
        }
        if (query != null && !query.isBlank()) {
            sql.append(" AND (LOWER(pc.name) LIKE :q OR LOWER(pc.slug) LIKE :q)");
            params.addValue("q", "%" + query.trim().toLowerCase() + "%");
        }
        sql.append(" ORDER BY pc.level ASC, pc.path ASC");

        return jdbcTemplate.query(sql.toString(), params, this::mapCategoryRow);
    }

    @Override
    public Optional<AdminCategoryRow> findCategoryById(UUID categoryId) {
        String sql = """
                SELECT pc.id,
                       pc.name,
                       pc.slug,
                       pc.parent_id,
                       pc.level,
                       pc.path,
                       pc.is_active,
                       pc.created_at,
                       pc.updated_at,
                       COALESCE((
                           SELECT COUNT(*)
                           FROM products p
                           WHERE p.category_id = pc.id
                       ), 0) AS product_count
                FROM product_categories pc
                WHERE pc.id = :categoryId
                """;
        List<AdminCategoryRow> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("categoryId", categoryId),
                this::mapCategoryRow
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public boolean existsCategorySlug(String slug, UUID excludeId) {
        String sql = """
                SELECT EXISTS(
                    SELECT 1
                    FROM product_categories
                    WHERE slug = :slug
                      AND (:excludeId IS NULL OR id <> :excludeId)
                )
                """;
        Boolean exists = jdbcTemplate.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("slug", slug)
                        .addValue("excludeId", excludeId),
                Boolean.class
        );
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public boolean hasActiveChildren(UUID categoryId) {
        String sql = """
                SELECT EXISTS(
                    SELECT 1
                    FROM product_categories
                    WHERE parent_id = :categoryId
                      AND is_active = TRUE
                )
                """;
        Boolean exists = jdbcTemplate.queryForObject(
                sql,
                new MapSqlParameterSource("categoryId", categoryId),
                Boolean.class
        );
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public long countProductsByCategoryId(UUID categoryId) {
        String sql = "SELECT COUNT(*) FROM products WHERE category_id = :categoryId";
        Long count = jdbcTemplate.queryForObject(
                sql,
                new MapSqlParameterSource("categoryId", categoryId),
                Long.class
        );
        return count == null ? 0L : count;
    }

    @Override
    public UUID insertCategory(
            UUID id,
            String name,
            String slug,
            UUID parentId,
            int level,
            String path,
            Instant now
    ) {
        String sql = """
                INSERT INTO product_categories (
                    id, name, slug, parent_id, is_active, level, path, created_at, updated_at
                ) VALUES (
                    :id, :name, :slug, :parentId, TRUE, :level, :path, :now, :now
                )
                """;
        jdbcTemplate.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("name", name)
                        .addValue("slug", slug)
                        .addValue("parentId", parentId)
                        .addValue("level", level)
                        .addValue("path", path)
                        .addValue("now", Timestamp.from(now))
        );
        return id;
    }

    @Override
    public void updateCategory(UUID id, String name, String slug, UUID parentId, int level, String path, Instant now) {
        String sql = """
                UPDATE product_categories
                SET name = :name,
                    slug = :slug,
                    parent_id = :parentId,
                    level = :level,
                    path = :path,
                    updated_at = :now
                WHERE id = :id
                """;
        jdbcTemplate.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("name", name)
                        .addValue("slug", slug)
                        .addValue("parentId", parentId)
                        .addValue("level", level)
                        .addValue("path", path)
                        .addValue("now", Timestamp.from(now))
        );
    }

    @Override
    public void updateCategorySubtreePaths(String oldPathPrefix, String newPathPrefix, int levelDelta, Instant now) {
        String sql = """
                UPDATE product_categories
                SET path = :newPathPrefix || SUBSTRING(path FROM LENGTH(:oldPathPrefix) + 1),
                    level = level + :levelDelta,
                    updated_at = :now
                WHERE path LIKE :oldPathPrefix || '%'
                """;
        jdbcTemplate.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("oldPathPrefix", oldPathPrefix)
                        .addValue("newPathPrefix", newPathPrefix)
                        .addValue("levelDelta", levelDelta)
                        .addValue("now", Timestamp.from(now))
        );
    }

    @Override
    public void setCategoryActive(UUID id, boolean active, Instant now) {
        String sql = """
                UPDATE product_categories
                SET is_active = :active,
                    updated_at = :now
                WHERE id = :id
                """;
        jdbcTemplate.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("active", active)
                        .addValue("now", Timestamp.from(now))
        );
    }

    @Override
    public List<AdminBrandRow> findBrands(Boolean activeOnly, String query, int page, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT b.id,
                       b.name,
                       b.slug,
                       b.is_active,
                       b.created_at,
                       b.updated_at,
                       COALESCE((
                           SELECT COUNT(*)
                           FROM products p
                           WHERE p.brand_id = b.id
                       ), 0) AS product_count
                FROM brands b
                WHERE 1 = 1
                """);

        MapSqlParameterSource params = new MapSqlParameterSource();
        appendBrandFilters(sql, params, activeOnly, query);
        sql.append(" ORDER BY b.name ASC");
        sql.append(" LIMIT :limit OFFSET :offset");
        params.addValue("limit", limit);
        params.addValue("offset", Math.max(0, (page - 1) * limit));

        return jdbcTemplate.query(sql.toString(), params, this::mapBrandRow);
    }

    @Override
    public long countBrands(Boolean activeOnly, String query) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM brands b WHERE 1 = 1");
        MapSqlParameterSource params = new MapSqlParameterSource();
        appendBrandFilters(sql, params, activeOnly, query);
        Long count = jdbcTemplate.queryForObject(sql.toString(), params, Long.class);
        return count == null ? 0L : count;
    }

    @Override
    public Optional<AdminBrandRow> findBrandById(UUID brandId) {
        String sql = """
                SELECT b.id,
                       b.name,
                       b.slug,
                       b.is_active,
                       b.created_at,
                       b.updated_at,
                       COALESCE((
                           SELECT COUNT(*)
                           FROM products p
                           WHERE p.brand_id = b.id
                       ), 0) AS product_count
                FROM brands b
                WHERE b.id = :brandId
                """;
        List<AdminBrandRow> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("brandId", brandId),
                this::mapBrandRow
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public boolean existsBrandSlug(String slug, UUID excludeId) {
        String sql = """
                SELECT EXISTS(
                    SELECT 1
                    FROM brands
                    WHERE slug = :slug
                      AND (:excludeId IS NULL OR id <> :excludeId)
                )
                """;
        Boolean exists = jdbcTemplate.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("slug", slug)
                        .addValue("excludeId", excludeId),
                Boolean.class
        );
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public UUID insertBrand(UUID id, String name, String slug, Instant now) {
        String sql = """
                INSERT INTO brands (id, name, slug, is_active, created_at, updated_at)
                VALUES (:id, :name, :slug, TRUE, :now, :now)
                """;
        jdbcTemplate.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("name", name)
                        .addValue("slug", slug)
                        .addValue("now", Timestamp.from(now))
        );
        return id;
    }

    @Override
    public void updateBrand(UUID id, String name, String slug, Instant now) {
        String sql = """
                UPDATE brands
                SET name = :name,
                    slug = :slug,
                    updated_at = :now
                WHERE id = :id
                """;
        jdbcTemplate.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("name", name)
                        .addValue("slug", slug)
                        .addValue("now", Timestamp.from(now))
        );
    }

    @Override
    public void setBrandActive(UUID id, boolean active, Instant now) {
        String sql = """
                UPDATE brands
                SET is_active = :active,
                    updated_at = :now
                WHERE id = :id
                """;
        jdbcTemplate.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("active", active)
                        .addValue("now", Timestamp.from(now))
        );
    }

    private void appendBrandFilters(
            StringBuilder sql,
            MapSqlParameterSource params,
            Boolean activeOnly,
            String query
    ) {
        if (Boolean.TRUE.equals(activeOnly)) {
            sql.append(" AND b.is_active = TRUE");
        } else if (Boolean.FALSE.equals(activeOnly)) {
            sql.append(" AND b.is_active = FALSE");
        }
        if (query != null && !query.isBlank()) {
            sql.append(" AND (LOWER(b.name) LIKE :q OR LOWER(b.slug) LIKE :q)");
            params.addValue("q", "%" + query.trim().toLowerCase() + "%");
        }
    }

    private AdminCategoryRow mapCategoryRow(ResultSet rs, int rowNum) throws SQLException {
        String parentId = rs.getString("parent_id");
        return new AdminCategoryRow(
                UUID.fromString(rs.getString("id")),
                rs.getString("name"),
                rs.getString("slug"),
                parentId == null ? null : UUID.fromString(parentId),
                rs.getInt("level"),
                rs.getString("path"),
                rs.getBoolean("is_active"),
                rs.getLong("product_count"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }

    private AdminBrandRow mapBrandRow(ResultSet rs, int rowNum) throws SQLException {
        return new AdminBrandRow(
                UUID.fromString(rs.getString("id")),
                rs.getString("name"),
                rs.getString("slug"),
                rs.getBoolean("is_active"),
                rs.getLong("product_count"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }
}
