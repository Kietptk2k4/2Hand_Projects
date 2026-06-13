package com.twohands.commerce_service.infrastructure.persistence.shop;

import com.twohands.commerce_service.domain.shop.PublicShopByUserSnapshot;
import com.twohands.commerce_service.domain.shop.ViewPublicShopByUserRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ViewPublicShopByUserRepositoryAdapter implements ViewPublicShopByUserRepository {

    private static final String ACTIVE_SHOP_BY_SELLER_SQL = """
            SELECT s.id AS shop_id,
                   s.shop_name,
                   s.avatar_url,
                   s.seller_id
            FROM seller_shops s
            WHERE s.seller_id = :sellerId
              AND s.status = 'ACTIVE'
            LIMIT 1
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ViewPublicShopByUserRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<PublicShopByUserSnapshot> findActiveShopBySellerId(UUID sellerId) {
        List<PublicShopByUserSnapshot> rows = jdbcTemplate.query(
                ACTIVE_SHOP_BY_SELLER_SQL,
                new MapSqlParameterSource("sellerId", sellerId),
                this::mapSnapshot
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    private PublicShopByUserSnapshot mapSnapshot(ResultSet rs, int rowNum) throws SQLException {
        return new PublicShopByUserSnapshot(
                true,
                UUID.fromString(rs.getString("shop_id")),
                rs.getString("shop_name"),
                rs.getString("avatar_url"),
                UUID.fromString(rs.getString("seller_id"))
        );
    }
}
