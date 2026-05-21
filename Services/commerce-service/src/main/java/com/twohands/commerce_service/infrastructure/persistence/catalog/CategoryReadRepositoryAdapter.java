package com.twohands.commerce_service.infrastructure.persistence.catalog;

import com.twohands.commerce_service.domain.catalog.ActiveCategory;
import com.twohands.commerce_service.domain.catalog.CategoryReadRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

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
}
