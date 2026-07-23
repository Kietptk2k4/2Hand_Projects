package com.twohands.commerce_service.infrastructure.persistence.product;

import com.twohands.commerce_service.domain.product.ProductForModeration;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.product.RemoveProductByAdminRepository;
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
public class RemoveProductByAdminRepositoryAdapter implements RemoveProductByAdminRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public RemoveProductByAdminRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ProductForModeration> findById(UUID productId) {
        String sql = """
                SELECT id, seller_id, shop_id, title, status::text AS status
                FROM products
                WHERE id = :productId
                """;
        List<ProductForModeration> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("productId", productId),
                this::mapProduct
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public boolean updateStatusToRemoved(
            UUID productId,
            ProductStatus currentStatus,
            Instant occurredAt,
            String reason
    ) {
        String sql = """
                UPDATE products
                SET status = CAST(:newStatus AS product_status),
                    remove_reason = :reason,
                    updated_at = :now
                WHERE id = :productId
                  AND status = CAST(:currentStatus AS product_status)
                """;
        int updated = jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("productId", productId)
                .addValue("currentStatus", currentStatus.name())
                .addValue("newStatus", ProductStatus.REMOVED.name())
                .addValue("reason", reason)
                .addValue("now", Timestamp.from(occurredAt)));
        return updated == 1;
    }

    private ProductForModeration mapProduct(ResultSet rs, int rowNum) throws SQLException {
        return new ProductForModeration(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("seller_id")),
                UUID.fromString(rs.getString("shop_id")),
                rs.getString("title"),
                ProductStatus.valueOf(rs.getString("status"))
        );
    }
}
