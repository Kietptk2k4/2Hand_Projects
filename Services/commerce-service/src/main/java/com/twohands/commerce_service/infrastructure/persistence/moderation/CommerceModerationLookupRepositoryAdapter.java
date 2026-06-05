package com.twohands.commerce_service.infrastructure.persistence.moderation;

import com.twohands.commerce_service.domain.moderation.CommerceModerationLookupRepository;
import com.twohands.commerce_service.domain.moderation.ProductModerationOwner;
import com.twohands.commerce_service.domain.moderation.ReviewModerationParties;
import com.twohands.commerce_service.domain.moderation.ShopModerationOwner;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CommerceModerationLookupRepositoryAdapter implements CommerceModerationLookupRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public CommerceModerationLookupRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ProductModerationOwner> findProductOwner(UUID productId) {
        String sql = """
                SELECT id, seller_id, shop_id
                FROM products
                WHERE id = :productId
                """;
        List<ProductModerationOwner> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("productId", productId),
                this::mapProductOwner
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public Optional<ShopModerationOwner> findShopOwner(UUID shopId) {
        String sql = """
                SELECT id, seller_id
                FROM seller_shops
                WHERE id = :shopId
                """;
        List<ShopModerationOwner> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("shopId", shopId),
                this::mapShopOwner
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public Optional<ReviewModerationParties> findReviewParties(UUID reviewId) {
        String sql = """
                SELECT id, seller_id, buyer_id
                FROM reviews
                WHERE id = :reviewId
                """;
        List<ReviewModerationParties> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("reviewId", reviewId),
                this::mapReviewParties
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    private ProductModerationOwner mapProductOwner(ResultSet rs, int rowNum) throws SQLException {
        return new ProductModerationOwner(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("seller_id")),
                UUID.fromString(rs.getString("shop_id"))
        );
    }

    private ShopModerationOwner mapShopOwner(ResultSet rs, int rowNum) throws SQLException {
        return new ShopModerationOwner(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("seller_id"))
        );
    }

    private ReviewModerationParties mapReviewParties(ResultSet rs, int rowNum) throws SQLException {
        return new ReviewModerationParties(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("seller_id")),
                UUID.fromString(rs.getString("buyer_id"))
        );
    }
}
