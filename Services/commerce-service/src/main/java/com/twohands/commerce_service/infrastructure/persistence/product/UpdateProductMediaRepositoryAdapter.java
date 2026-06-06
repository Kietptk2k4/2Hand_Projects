package com.twohands.commerce_service.infrastructure.persistence.product;

import com.twohands.commerce_service.domain.product.ProductMediaItem;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.product.UpdateProductMediaProductRef;
import com.twohands.commerce_service.domain.product.UpdateProductMediaRepository;
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
public class UpdateProductMediaRepositoryAdapter implements UpdateProductMediaRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public UpdateProductMediaRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<UpdateProductMediaProductRef> findProductByIdAndSellerId(UUID productId, UUID sellerId) {
        String sql = """
                SELECT id, seller_id, shop_id, status::text AS product_status
                FROM products
                WHERE id = :productId AND seller_id = :sellerId
                """;
        List<UpdateProductMediaProductRef> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("productId", productId)
                        .addValue("sellerId", sellerId),
                this::mapProductRef
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public List<ProductMediaItem> replaceMedia(UUID productId, List<ProductMediaItem> mediaItems) {
        jdbcTemplate.update(
                "DELETE FROM product_media WHERE product_id = :productId",
                new MapSqlParameterSource("productId", productId)
        );

        if (!mediaItems.isEmpty()) {
            SqlParameterSource[] batch = mediaItems.stream()
                    .map(item -> new MapSqlParameterSource()
                            .addValue("id", UUID.randomUUID())
                            .addValue("productId", productId)
                            .addValue("mediaUrl", item.mediaUrl())
                            .addValue("mediaType", item.mediaType())
                            .addValue("sortOrder", item.sortOrder()))
                    .toArray(SqlParameterSource[]::new);

            jdbcTemplate.batchUpdate(
                    """
                            INSERT INTO product_media (id, product_id, media_url, media_type, sort_order)
                            VALUES (:id, :productId, :mediaUrl, :mediaType, :sortOrder)
                            """,
                    batch
            );
        }

        return loadMedia(productId);
    }

    private List<ProductMediaItem> loadMedia(UUID productId) {
        return jdbcTemplate.query(
                """
                        SELECT media_url, media_type, sort_order
                        FROM product_media
                        WHERE product_id = :productId
                        ORDER BY sort_order ASC, created_at ASC
                        """,
                new MapSqlParameterSource("productId", productId),
                (rs, rowNum) -> new ProductMediaItem(
                        rs.getString("media_url"),
                        rs.getString("media_type"),
                        rs.getInt("sort_order")
                )
        );
    }

    private UpdateProductMediaProductRef mapProductRef(ResultSet rs, int rowNum) throws SQLException {
        return new UpdateProductMediaProductRef(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("seller_id")),
                UUID.fromString(rs.getString("shop_id")),
                ProductStatus.valueOf(rs.getString("product_status"))
        );
    }
}
