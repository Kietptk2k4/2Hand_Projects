package com.twohands.commerce_service.infrastructure.persistence.product;

import com.twohands.commerce_service.domain.product.UpdateProductDraft;
import com.twohands.commerce_service.domain.product.UpdateProductRepository;
import com.twohands.commerce_service.domain.product.UpdateProductResult;
import com.twohands.commerce_service.domain.product.UpdateProductSnapshot;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
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
public class UpdateProductRepositoryAdapter implements UpdateProductRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public UpdateProductRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<UpdateProductSnapshot> findByIdAndSellerId(UUID productId, UUID sellerId) {
        String sql = """
                SELECT id, seller_id, shop_id, status::text AS product_status,
                       product_type, category_id, brand_id, condition, title, description,
                       weight_gram, created_at
                FROM products
                WHERE id = :productId AND seller_id = :sellerId
                """;
        List<UpdateProductSnapshot> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("productId", productId)
                        .addValue("sellerId", sellerId),
                this::mapSnapshot
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public UpdateProductResult update(UpdateProductDraft draft, Instant occurredAt) {
        String sql = """
                UPDATE products
                SET product_type = :productType,
                    category_id = :categoryId,
                    brand_id = :brandId,
                    condition = :condition,
                    title = :title,
                    description = :description,
                    weight_gram = :weightGram,
                    updated_at = :now
                WHERE id = :productId
                  AND seller_id = :sellerId
                """;
        int updated = jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("productId", draft.productId())
                .addValue("sellerId", draft.sellerId())
                .addValue("productType", draft.productType())
                .addValue("categoryId", draft.categoryId())
                .addValue("brandId", draft.brandId())
                .addValue("condition", draft.condition())
                .addValue("title", draft.title())
                .addValue("description", draft.description())
                .addValue("weightGram", draft.weightGram())
                .addValue("now", Timestamp.from(occurredAt)));

        if (updated == 0) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        return loadResult(draft.productId(), draft.sellerId(), occurredAt);
    }

    private UpdateProductResult loadResult(UUID productId, UUID sellerId, Instant updatedAt) {
        return findByIdAndSellerId(productId, sellerId)
                .map(snapshot -> new UpdateProductResult(
                        snapshot.productId(),
                        snapshot.sellerId(),
                        snapshot.shopId(),
                        snapshot.status(),
                        snapshot.productType(),
                        snapshot.categoryId(),
                        snapshot.brandId(),
                        snapshot.condition(),
                        snapshot.title(),
                        snapshot.description(),
                        snapshot.weightGram(),
                        snapshot.createdAt(),
                        updatedAt
                ))
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private UpdateProductSnapshot mapSnapshot(ResultSet rs, int rowNum) throws SQLException {
        return new UpdateProductSnapshot(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("seller_id")),
                UUID.fromString(rs.getString("shop_id")),
                ProductStatus.valueOf(rs.getString("product_status")),
                rs.getString("product_type"),
                UUID.fromString(rs.getString("category_id")),
                optionalUuid(rs.getString("brand_id")),
                rs.getString("condition"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getInt("weight_gram"),
                rs.getTimestamp("created_at").toInstant()
        );
    }

    private static UUID optionalUuid(String value) {
        return value == null ? null : UUID.fromString(value);
    }
}
