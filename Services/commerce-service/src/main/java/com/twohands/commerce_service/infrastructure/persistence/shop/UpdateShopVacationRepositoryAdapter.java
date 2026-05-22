package com.twohands.commerce_service.infrastructure.persistence.shop;

import com.twohands.commerce_service.domain.shop.ShopStatus;
import com.twohands.commerce_service.domain.shop.UpdateShopVacationDraft;
import com.twohands.commerce_service.domain.shop.UpdateShopVacationRepository;
import com.twohands.commerce_service.domain.shop.UpdateShopVacationResult;
import com.twohands.commerce_service.domain.shop.UpdateShopVacationSnapshot;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
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
public class UpdateShopVacationRepositoryAdapter implements UpdateShopVacationRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public UpdateShopVacationRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<UpdateShopVacationSnapshot> findBySellerId(UUID sellerId) {
        String sql = """
                SELECT s.id,
                       s.seller_id,
                       s.status::text AS shop_status,
                       COALESCE(st.is_vacation, FALSE) AS is_vacation,
                       st.vacation_message
                FROM seller_shops s
                LEFT JOIN shop_settings st ON st.shop_id = s.id
                WHERE s.seller_id = :sellerId
                """;
        List<UpdateShopVacationSnapshot> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("sellerId", sellerId),
                (rs, rowNum) -> new UpdateShopVacationSnapshot(
                        UUID.fromString(rs.getString("id")),
                        UUID.fromString(rs.getString("seller_id")),
                        ShopStatus.valueOf(rs.getString("shop_status")),
                        rs.getBoolean("is_vacation"),
                        rs.getString("vacation_message")
                )
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public UpdateShopVacationResult updateVacationSettings(UpdateShopVacationDraft draft, Instant updatedAt) {
        ensureShopSettingsRow(draft.shopId(), updatedAt);

        int updated = jdbcTemplate.update(
                """
                        UPDATE shop_settings
                        SET is_vacation = :isVacation,
                            vacation_message = :vacationMessage,
                            updated_at = :updatedAt
                        WHERE shop_id = :shopId
                        """,
                new MapSqlParameterSource()
                        .addValue("isVacation", draft.isVacation())
                        .addValue("vacationMessage", draft.vacationMessage())
                        .addValue("updatedAt", Timestamp.from(updatedAt))
                        .addValue("shopId", draft.shopId())
        );

        if (updated == 0) {
            throw new AppException(ErrorCode.SHOP_NOT_FOUND);
        }

        return loadResult(draft.shopId(), updatedAt);
    }

    private void ensureShopSettingsRow(UUID shopId, Instant updatedAt) {
        jdbcTemplate.update(
                """
                        INSERT INTO shop_settings (shop_id, is_vacation, vacation_message, updated_at)
                        VALUES (:shopId, FALSE, NULL, :updatedAt)
                        ON CONFLICT (shop_id) DO NOTHING
                        """,
                new MapSqlParameterSource()
                        .addValue("shopId", shopId)
                        .addValue("updatedAt", Timestamp.from(updatedAt))
        );
    }

    private UpdateShopVacationResult loadResult(UUID shopId, Instant updatedAt) {
        return jdbcTemplate.queryForObject(
                """
                        SELECT s.id,
                               s.seller_id,
                               s.status::text AS shop_status,
                               st.is_vacation,
                               st.vacation_message
                        FROM seller_shops s
                        INNER JOIN shop_settings st ON st.shop_id = s.id
                        WHERE s.id = :shopId
                        """,
                new MapSqlParameterSource("shopId", shopId),
                (rs, rowNum) -> mapResult(rs, updatedAt)
        );
    }

    private UpdateShopVacationResult mapResult(ResultSet rs, Instant updatedAt) throws SQLException {
        return new UpdateShopVacationResult(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("seller_id")),
                ShopStatus.valueOf(rs.getString("shop_status")),
                rs.getBoolean("is_vacation"),
                rs.getString("vacation_message"),
                updatedAt
        );
    }
}
