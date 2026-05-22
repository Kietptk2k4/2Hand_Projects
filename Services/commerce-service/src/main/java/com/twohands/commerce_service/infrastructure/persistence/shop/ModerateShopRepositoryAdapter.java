package com.twohands.commerce_service.infrastructure.persistence.shop;

import com.twohands.commerce_service.domain.shop.ModerateShopRepository;
import com.twohands.commerce_service.domain.shop.ShopForModeration;
import com.twohands.commerce_service.domain.shop.ShopStatus;
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
public class ModerateShopRepositoryAdapter implements ModerateShopRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ModerateShopRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ShopForModeration> findById(UUID shopId) {
        String sql = """
                SELECT id, seller_id, shop_name, status::text AS status
                FROM seller_shops
                WHERE id = :shopId
                """;
        List<ShopForModeration> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("shopId", shopId),
                this::mapShop
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public boolean updateStatus(
            UUID shopId,
            ShopStatus currentStatus,
            ShopStatus newStatus,
            Instant occurredAt
    ) {
        String sql = """
                UPDATE seller_shops
                SET status = CAST(:newStatus AS shop_status),
                    updated_at = :now
                WHERE id = :shopId
                  AND status = CAST(:currentStatus AS shop_status)
                """;
        int updated = jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("shopId", shopId)
                .addValue("currentStatus", currentStatus.name())
                .addValue("newStatus", newStatus.name())
                .addValue("now", Timestamp.from(occurredAt)));
        return updated == 1;
    }

    private ShopForModeration mapShop(ResultSet rs, int rowNum) throws SQLException {
        return new ShopForModeration(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("seller_id")),
                rs.getString("shop_name"),
                ShopStatus.valueOf(rs.getString("status"))
        );
    }
}
