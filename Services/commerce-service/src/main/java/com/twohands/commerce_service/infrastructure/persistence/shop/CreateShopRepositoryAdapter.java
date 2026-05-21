package com.twohands.commerce_service.infrastructure.persistence.shop;

import com.twohands.commerce_service.domain.shop.CreateShopDraft;
import com.twohands.commerce_service.domain.shop.CreateShopPickupDraft;
import com.twohands.commerce_service.domain.shop.CreateShopRepository;
import com.twohands.commerce_service.domain.shop.CreateShopResult;
import com.twohands.commerce_service.domain.shop.ShopStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Repository
public class CreateShopRepositoryAdapter implements CreateShopRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public CreateShopRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean existsBySellerId(UUID sellerId) {
        String sql = "SELECT COUNT(1) FROM seller_shops WHERE seller_id = :sellerId";
        Integer count = jdbcTemplate.queryForObject(
                sql,
                new MapSqlParameterSource("sellerId", sellerId),
                Integer.class
        );
        return count != null && count > 0;
    }

    @Override
    public CreateShopResult create(CreateShopDraft draft, Instant occurredAt) {
        UUID shopId = UUID.randomUUID();
        try {
            insertShop(shopId, draft, occurredAt);
            insertShopSettings(shopId, occurredAt);
            boolean shippingProfileCreated = false;
            if (draft.hasPickupProfile()) {
                insertShippingProfile(shopId, draft.pickupProfile());
                shippingProfileCreated = true;
            }
            return new CreateShopResult(
                    shopId,
                    draft.sellerId(),
                    draft.shopName(),
                    draft.description(),
                    draft.avatarUrl(),
                    draft.coverUrl(),
                    ShopStatus.ACTIVE,
                    false,
                    shippingProfileCreated,
                    occurredAt,
                    occurredAt
            );
        } catch (DataIntegrityViolationException ex) {
            throw new AppException(ErrorCode.SHOP_ALREADY_EXISTS, "Seller already has a shop", ex);
        }
    }

    private void insertShop(UUID shopId, CreateShopDraft draft, Instant occurredAt) {
        String sql = """
                INSERT INTO seller_shops(
                    id, seller_id, shop_name, description, avatar_url, cover_url,
                    status, rating_avg, rating_count, created_at, updated_at
                ) VALUES (
                    :shopId, :sellerId, :shopName, :description, :avatarUrl, :coverUrl,
                    CAST(:status AS shop_status), 0, 0, :now, :now
                )
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("shopId", shopId)
                .addValue("sellerId", draft.sellerId())
                .addValue("shopName", draft.shopName())
                .addValue("description", draft.description())
                .addValue("avatarUrl", draft.avatarUrl())
                .addValue("coverUrl", draft.coverUrl())
                .addValue("status", ShopStatus.ACTIVE.name())
                .addValue("now", Timestamp.from(occurredAt)));
    }

    private void insertShopSettings(UUID shopId, Instant occurredAt) {
        String sql = """
                INSERT INTO shop_settings(shop_id, is_vacation, vacation_message, updated_at)
                VALUES (:shopId, FALSE, NULL, :now)
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("shopId", shopId)
                .addValue("now", Timestamp.from(occurredAt)));
    }

    private void insertShippingProfile(UUID shopId, CreateShopPickupDraft pickup) {
        String sql = """
                INSERT INTO seller_shipping_profiles(
                    shop_id, pickup_name, phone, province_code, district_code, ward_code, address_detail
                ) VALUES (
                    :shopId, :pickupName, :phone, :provinceCode, :districtCode, :wardCode, :addressDetail
                )
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("shopId", shopId)
                .addValue("pickupName", pickup.pickupName().trim())
                .addValue("phone", pickup.phone().trim())
                .addValue("provinceCode", pickup.provinceCode().trim())
                .addValue("districtCode", pickup.districtCode().trim())
                .addValue("wardCode", pickup.wardCode().trim())
                .addValue("addressDetail", pickup.addressDetail().trim()));
    }
}
