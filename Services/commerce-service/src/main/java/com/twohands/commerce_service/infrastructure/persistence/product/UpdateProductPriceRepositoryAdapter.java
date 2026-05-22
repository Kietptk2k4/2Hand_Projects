package com.twohands.commerce_service.infrastructure.persistence.product;

import com.twohands.commerce_service.domain.catalog.ProductPriceCalculator;
import com.twohands.commerce_service.domain.product.OverlappingProductPrice;
import com.twohands.commerce_service.domain.product.ProductPriceRecord;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.product.UpdateProductPriceDraft;
import com.twohands.commerce_service.domain.product.UpdateProductPriceProductRef;
import com.twohands.commerce_service.domain.product.UpdateProductPriceRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UpdateProductPriceRepositoryAdapter implements UpdateProductPriceRepository {

    private static final Instant OPEN_END = Instant.parse("9999-12-31T23:59:59Z");

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public UpdateProductPriceRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<UpdateProductPriceProductRef> findProductByIdAndSellerId(UUID productId, UUID sellerId) {
        String sql = """
                SELECT id, seller_id, shop_id, status::text AS product_status
                FROM products
                WHERE id = :productId AND seller_id = :sellerId
                """;
        List<UpdateProductPriceProductRef> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("productId", productId)
                        .addValue("sellerId", sellerId),
                (rs, rowNum) -> new UpdateProductPriceProductRef(
                        UUID.fromString(rs.getString("id")),
                        UUID.fromString(rs.getString("seller_id")),
                        UUID.fromString(rs.getString("shop_id")),
                        ProductStatus.valueOf(rs.getString("product_status"))
                )
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public List<OverlappingProductPrice> findOverlappingPrices(UUID productId, Instant startAt, Instant endAt) {
        Instant effectiveEnd = endAt == null ? OPEN_END : endAt;
        return jdbcTemplate.query(
                """
                        SELECT id, start_at, end_at
                        FROM product_prices
                        WHERE product_id = :productId
                          AND start_at < :newEnd
                          AND COALESCE(end_at, :openEnd) > :newStart
                        ORDER BY start_at ASC
                        """,
                new MapSqlParameterSource()
                        .addValue("productId", productId)
                        .addValue("newStart", Timestamp.from(startAt))
                        .addValue("newEnd", Timestamp.from(effectiveEnd))
                        .addValue("openEnd", Timestamp.from(OPEN_END)),
                (rs, rowNum) -> new OverlappingProductPrice(
                        UUID.fromString(rs.getString("id")),
                        rs.getTimestamp("start_at").toInstant(),
                        rs.getTimestamp("end_at") == null ? null : rs.getTimestamp("end_at").toInstant()
                )
        );
    }

    @Override
    public int closePricesAtStart(UUID productId, List<UUID> priceIds, Instant endAt) {
        if (priceIds.isEmpty()) {
            return 0;
        }
        return jdbcTemplate.update(
                """
                        UPDATE product_prices
                        SET end_at = :endAt
                        WHERE product_id = :productId
                          AND id IN (:priceIds)
                          AND start_at < :endAt
                          AND (end_at IS NULL OR end_at > :endAt)
                        """,
                new MapSqlParameterSource()
                        .addValue("productId", productId)
                        .addValue("priceIds", priceIds)
                        .addValue("endAt", Timestamp.from(endAt))
        );
    }

    @Override
    public ProductPriceRecord insertPrice(UpdateProductPriceDraft draft, Instant createdAt) {
        UUID priceId = UUID.randomUUID();
        jdbcTemplate.update(
                """
                        INSERT INTO product_prices (
                            id, product_id, price, sale_price, start_at, end_at, created_at
                        )
                        VALUES (
                            :id, :productId, :price, :salePrice, :startAt, :endAt, :createdAt
                        )
                        """,
                new MapSqlParameterSource()
                        .addValue("id", priceId)
                        .addValue("productId", draft.productId())
                        .addValue("price", draft.price())
                        .addValue("salePrice", draft.salePrice())
                        .addValue("startAt", Timestamp.from(draft.startAt()))
                        .addValue("endAt", draft.endAt() == null ? null : Timestamp.from(draft.endAt()))
                        .addValue("createdAt", Timestamp.from(createdAt))
        );

        return loadPrice(priceId);
    }

    private ProductPriceRecord loadPrice(UUID priceId) {
        return jdbcTemplate.queryForObject(
                """
                        SELECT id, product_id, price, sale_price, start_at, end_at, created_at
                        FROM product_prices
                        WHERE id = :priceId
                        """,
                new MapSqlParameterSource("priceId", priceId),
                this::mapPriceRecord
        );
    }

    private ProductPriceRecord mapPriceRecord(ResultSet rs, int rowNum) throws SQLException {
        BigDecimal price = rs.getBigDecimal("price");
        BigDecimal salePrice = rs.getBigDecimal("sale_price");
        return new ProductPriceRecord(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("product_id")),
                price,
                salePrice,
                ProductPriceCalculator.effectivePrice(price, salePrice),
                rs.getTimestamp("start_at").toInstant(),
                rs.getTimestamp("end_at") == null ? null : rs.getTimestamp("end_at").toInstant(),
                rs.getTimestamp("created_at").toInstant()
        );
    }
}
