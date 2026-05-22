package com.twohands.commerce_service.infrastructure.persistence.shop;

import com.twohands.commerce_service.domain.shop.ShopStatus;
import com.twohands.commerce_service.domain.shop.UpdateShopProfileDraft;
import com.twohands.commerce_service.domain.shop.UpdateShopProfileRepository;
import com.twohands.commerce_service.domain.shop.UpdateShopProfileResult;
import com.twohands.commerce_service.domain.shop.UpdateShopProfileSnapshot;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UpdateShopProfileRepositoryAdapter implements UpdateShopProfileRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public UpdateShopProfileRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<UpdateShopProfileSnapshot> findBySellerId(UUID sellerId) {
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
                       COALESCE(st.is_vacation, FALSE) AS is_vacation
                FROM seller_shops s
                LEFT JOIN shop_settings st ON st.shop_id = s.id
                WHERE s.seller_id = :sellerId
                """;
        List<UpdateShopProfileSnapshot> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("sellerId", sellerId),
                this::mapSnapshot
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public UpdateShopProfileResult updateProfile(UpdateShopProfileDraft draft, Instant updatedAt) {
        String sql = """
                UPDATE seller_shops
                SET shop_name = :shopName,
                    description = :description,
                    avatar_url = :avatarUrl,
                    cover_url = :coverUrl,
                    updated_at = :updatedAt
                WHERE id = :shopId
                  AND seller_id = :sellerId
                """;
        int updated = jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("shopName", draft.shopName())
                .addValue("description", draft.description())
                .addValue("avatarUrl", draft.avatarUrl())
                .addValue("coverUrl", draft.coverUrl())
                .addValue("updatedAt", Timestamp.from(updatedAt))
                .addValue("shopId", draft.shopId())
                .addValue("sellerId", draft.sellerId()));

        if (updated == 0) {
            throw new AppException(ErrorCode.SHOP_NOT_FOUND);
        }

        return loadResult(draft.sellerId(), updatedAt);
    }

    private UpdateShopProfileResult loadResult(UUID sellerId, Instant updatedAt) {
        return findBySellerId(sellerId)
                .map(snapshot -> new UpdateShopProfileResult(
                        snapshot.shopId(),
                        snapshot.sellerId(),
                        snapshot.shopName(),
                        snapshot.description(),
                        snapshot.avatarUrl(),
                        snapshot.coverUrl(),
                        snapshot.status(),
                        snapshot.ratingAvg(),
                        snapshot.ratingCount(),
                        snapshot.vacationMode(),
                        snapshot.createdAt(),
                        updatedAt
                ))
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND));
    }

    private UpdateShopProfileSnapshot mapSnapshot(ResultSet rs, int rowNum) throws SQLException {
        return new UpdateShopProfileSnapshot(
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
                rs.getTimestamp("created_at").toInstant()
        );
    }
}
