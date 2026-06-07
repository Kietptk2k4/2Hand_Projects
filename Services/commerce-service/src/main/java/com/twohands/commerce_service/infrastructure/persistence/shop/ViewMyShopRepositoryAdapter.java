package com.twohands.commerce_service.infrastructure.persistence.shop;

import com.twohands.commerce_service.domain.shop.ShopStatus;
import com.twohands.commerce_service.domain.shop.ViewMyShopRepository;
import com.twohands.commerce_service.domain.shop.ViewMyShopResult;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ViewMyShopRepositoryAdapter implements ViewMyShopRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ViewMyShopRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ViewMyShopResult> findBySellerId(UUID sellerId) {
        String sql = """
                SELECT s.id,
                       s.seller_id,
                       s.shop_name,
                       s.description,
                       s.avatar_url,
                       s.cover_url,
                       s.status::text AS shop_status,
                       s.rating_avg,
                       s.rating_count,
                       s.created_at,
                       s.updated_at,
                       COALESCE(st.is_vacation, FALSE) AS is_vacation,
                       st.vacation_message
                FROM seller_shops s
                LEFT JOIN shop_settings st ON st.shop_id = s.id
                WHERE s.seller_id = :sellerId
                """;
        List<ViewMyShopResult> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("sellerId", sellerId),
                this::mapResult
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    private ViewMyShopResult mapResult(ResultSet rs, int rowNum) throws SQLException {
        return new ViewMyShopResult(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("seller_id")),
                rs.getString("shop_name"),
                rs.getString("description"),
                rs.getString("avatar_url"),
                rs.getString("cover_url"),
                ShopStatus.valueOf(rs.getString("shop_status")),
                rs.getBigDecimal("rating_avg").setScale(2, RoundingMode.HALF_UP),
                rs.getInt("rating_count"),
                rs.getBoolean("is_vacation"),
                rs.getString("vacation_message"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }
}