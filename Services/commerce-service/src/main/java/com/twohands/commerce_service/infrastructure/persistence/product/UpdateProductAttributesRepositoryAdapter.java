package com.twohands.commerce_service.infrastructure.persistence.product;

import com.twohands.commerce_service.domain.product.ProductAttributeItem;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.product.UpdateProductAttributesProductRef;
import com.twohands.commerce_service.domain.product.UpdateProductAttributesRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UpdateProductAttributesRepositoryAdapter implements UpdateProductAttributesRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public UpdateProductAttributesRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<UpdateProductAttributesProductRef> findProductByIdAndSellerId(UUID productId, UUID sellerId) {
        String sql = """
                SELECT id, seller_id, shop_id, status::text AS product_status
                FROM products
                WHERE id = :productId AND seller_id = :sellerId
                """;
        List<UpdateProductAttributesProductRef> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("productId", productId)
                        .addValue("sellerId", sellerId),
                this::mapProductRef
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public List<ProductAttributeItem> replaceAttributes(UUID productId, List<ProductAttributeItem> attributes) {
        jdbcTemplate.update(
                "DELETE FROM product_attributes WHERE product_id = :productId",
                new MapSqlParameterSource("productId", productId)
        );

        if (!attributes.isEmpty()) {
            SqlParameterSource[] batch = attributes.stream()
                    .map(item -> new MapSqlParameterSource()
                            .addValue("id", UUID.randomUUID())
                            .addValue("productId", productId)
                            .addValue("attributeName", item.attributeName())
                            .addValue("attributeValue", item.attributeValue()))
                    .toArray(SqlParameterSource[]::new);

            jdbcTemplate.batchUpdate(
                    """
                            INSERT INTO product_attributes (id, product_id, attribute_name, attribute_value)
                            VALUES (:id, :productId, :attributeName, :attributeValue)
                            """,
                    batch
            );
        }

        return loadAttributes(productId);
    }

    private List<ProductAttributeItem> loadAttributes(UUID productId) {
        return jdbcTemplate.query(
                """
                        SELECT attribute_name, attribute_value
                        FROM product_attributes
                        WHERE product_id = :productId
                        ORDER BY attribute_name
                        """,
                new MapSqlParameterSource("productId", productId),
                (rs, rowNum) -> new ProductAttributeItem(
                        rs.getString("attribute_name"),
                        rs.getString("attribute_value")
                )
        );
    }

    private UpdateProductAttributesProductRef mapProductRef(ResultSet rs, int rowNum) throws SQLException {
        return new UpdateProductAttributesProductRef(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("seller_id")),
                UUID.fromString(rs.getString("shop_id")),
                ProductStatus.valueOf(rs.getString("product_status"))
        );
    }
}
