package com.twohands.commerce_service.infrastructure.persistence.shop;

import com.twohands.commerce_service.domain.shop.ShopVacationReadRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Repository
public class ShopVacationReadRepositoryAdapter implements ShopVacationReadRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ShopVacationReadRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Map<UUID, Boolean> findVacationByShopIds(Collection<UUID> shopIds) {
        if (shopIds == null || shopIds.isEmpty()) {
            return Map.of();
        }
        return jdbcTemplate.query(
                """
                        SELECT shop_id, COALESCE(is_vacation, FALSE) AS is_vacation
                        FROM shop_settings
                        WHERE shop_id IN (:shopIds)
                        """,
                new MapSqlParameterSource("shopIds", shopIds),
                rs -> {
                    Map<UUID, Boolean> result = new HashMap<>();
                    while (rs.next()) {
                        result.put(UUID.fromString(rs.getString("shop_id")), rs.getBoolean("is_vacation"));
                    }
                    return result;
                }
        );
    }
}
