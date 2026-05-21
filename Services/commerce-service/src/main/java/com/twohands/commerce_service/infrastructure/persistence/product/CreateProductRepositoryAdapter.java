package com.twohands.commerce_service.infrastructure.persistence.product;

import com.twohands.commerce_service.domain.product.CreateProductDraft;
import com.twohands.commerce_service.domain.product.CreateProductRepository;
import com.twohands.commerce_service.domain.product.CreateProductResult;
import com.twohands.commerce_service.domain.product.ProductStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Repository
public class CreateProductRepositoryAdapter implements CreateProductRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public CreateProductRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public CreateProductResult create(CreateProductDraft draft, Instant occurredAt) {
        UUID productId = UUID.randomUUID();
        String sql = """
                INSERT INTO products(
                    id, seller_id, shop_id, product_type, category_id, brand_id,
                    condition, title, description, weight_gram, status, created_at, updated_at
                ) VALUES (
                    :id, :sellerId, :shopId, :productType, :categoryId, :brandId,
                    :condition, :title, :description, :weightGram, CAST(:status AS product_status), :now, :now
                )
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", productId)
                .addValue("sellerId", draft.sellerId())
                .addValue("shopId", draft.shopId())
                .addValue("productType", draft.productType())
                .addValue("categoryId", draft.categoryId())
                .addValue("brandId", draft.brandId())
                .addValue("condition", draft.condition())
                .addValue("title", draft.title())
                .addValue("description", draft.description())
                .addValue("weightGram", draft.weightGram())
                .addValue("status", ProductStatus.DRAFT.name())
                .addValue("now", Timestamp.from(occurredAt)));

        return new CreateProductResult(
                productId,
                draft.sellerId(),
                draft.shopId(),
                ProductStatus.DRAFT,
                draft.productType(),
                draft.categoryId(),
                draft.brandId(),
                draft.condition(),
                draft.title(),
                draft.description(),
                draft.weightGram(),
                occurredAt,
                occurredAt
        );
    }
}
