package com.twohands.commerce_service.infrastructure.persistence.address;

import com.twohands.commerce_service.domain.address.CreateUserAddressDraft;
import com.twohands.commerce_service.domain.address.CreateUserAddressRepository;
import com.twohands.commerce_service.domain.address.CreateUserAddressResult;
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
public class CreateUserAddressRepositoryAdapter implements CreateUserAddressRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public CreateUserAddressRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean hasAnyAddress(UUID userId) {
        String sql = "SELECT COUNT(1) FROM user_addresses WHERE user_id = :userId";
        Integer count = jdbcTemplate.queryForObject(
                sql,
                new MapSqlParameterSource("userId", userId),
                Integer.class
        );
        return count != null && count > 0;
    }

    @Override
    public CreateUserAddressResult create(CreateUserAddressDraft draft, Instant occurredAt) {
        UUID addressId = UUID.randomUUID();
        try {
            if (draft.isDefault()) {
                clearDefaultAddresses(draft.userId(), occurredAt);
            }
            insertAddress(addressId, draft, occurredAt);
        } catch (DataIntegrityViolationException ex) {
            throw new AppException(
                    ErrorCode.ADDRESS_DEFAULT_CONFLICT,
                    "Default address conflict for user",
                    ex
            );
        }

        return new CreateUserAddressResult(
                addressId,
                draft.userId(),
                draft.receiverName(),
                draft.phone(),
                draft.provinceCode(),
                draft.districtCode(),
                draft.wardCode(),
                draft.addressDetail(),
                draft.isDefault(),
                occurredAt,
                occurredAt
        );
    }

    private void clearDefaultAddresses(UUID userId, Instant occurredAt) {
        String sql = """
                UPDATE user_addresses
                SET is_default = FALSE,
                    updated_at = :now
                WHERE user_id = :userId
                  AND is_default = TRUE
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("now", Timestamp.from(occurredAt)));
    }

    private void insertAddress(UUID addressId, CreateUserAddressDraft draft, Instant occurredAt) {
        String sql = """
                INSERT INTO user_addresses(
                    id, user_id, receiver_name, phone, province_code, district_code,
                    ward_code, address_detail, is_default, created_at, updated_at
                ) VALUES (
                    :addressId, :userId, :receiverName, :phone, :provinceCode, :districtCode,
                    :wardCode, :addressDetail, :isDefault, :now, :now
                )
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("addressId", addressId)
                .addValue("userId", draft.userId())
                .addValue("receiverName", draft.receiverName())
                .addValue("phone", draft.phone())
                .addValue("provinceCode", draft.provinceCode())
                .addValue("districtCode", draft.districtCode())
                .addValue("wardCode", draft.wardCode())
                .addValue("addressDetail", draft.addressDetail())
                .addValue("isDefault", draft.isDefault())
                .addValue("now", Timestamp.from(occurredAt)));
    }
}
