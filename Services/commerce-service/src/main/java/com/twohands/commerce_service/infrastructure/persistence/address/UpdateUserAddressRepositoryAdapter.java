package com.twohands.commerce_service.infrastructure.persistence.address;

import com.twohands.commerce_service.domain.address.UpdateUserAddressDraft;
import com.twohands.commerce_service.domain.address.UpdateUserAddressRepository;
import com.twohands.commerce_service.domain.address.UpdateUserAddressResult;
import com.twohands.commerce_service.domain.address.UpdateUserAddressSnapshot;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.dao.DataIntegrityViolationException;
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
public class UpdateUserAddressRepositoryAdapter implements UpdateUserAddressRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public UpdateUserAddressRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<UpdateUserAddressSnapshot> findByIdAndUserId(UUID addressId, UUID userId) {
        String sql = """
                SELECT id, user_id, receiver_name, phone, province_code, district_code,
                       ward_code, address_detail, is_default
                FROM user_addresses
                WHERE id = :addressId
                  AND user_id = :userId
                """;
        List<UpdateUserAddressSnapshot> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("addressId", addressId)
                        .addValue("userId", userId),
                this::mapSnapshot
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public UpdateUserAddressResult update(UpdateUserAddressDraft draft, Instant updatedAt) {
        try {
            if (draft.isDefault()) {
                clearDefaultAddresses(draft.userId(), updatedAt);
            }
            int updated = jdbcTemplate.update(
                    """
                            UPDATE user_addresses
                            SET receiver_name = :receiverName,
                                phone = :phone,
                                province_code = :provinceCode,
                                district_code = :districtCode,
                                ward_code = :wardCode,
                                address_detail = :addressDetail,
                                is_default = :isDefault,
                                updated_at = :updatedAt
                            WHERE id = :addressId
                              AND user_id = :userId
                            """,
                    new MapSqlParameterSource()
                            .addValue("receiverName", draft.receiverName())
                            .addValue("phone", draft.phone())
                            .addValue("provinceCode", draft.provinceCode())
                            .addValue("districtCode", draft.districtCode())
                            .addValue("wardCode", draft.wardCode())
                            .addValue("addressDetail", draft.addressDetail())
                            .addValue("isDefault", draft.isDefault())
                            .addValue("updatedAt", Timestamp.from(updatedAt))
                            .addValue("addressId", draft.addressId())
                            .addValue("userId", draft.userId())
            );
            if (updated == 0) {
                throw new AppException(ErrorCode.ADDRESS_NOT_FOUND);
            }
            return loadAddress(draft.addressId(), draft.userId());
        } catch (DataIntegrityViolationException ex) {
            throw new AppException(
                    ErrorCode.ADDRESS_DEFAULT_CONFLICT,
                    "Default address conflict while updating address",
                    ex
            );
        }
    }

    private void clearDefaultAddresses(UUID userId, Instant occurredAt) {
        jdbcTemplate.update(
                """
                        UPDATE user_addresses
                        SET is_default = FALSE,
                            updated_at = :now
                        WHERE user_id = :userId
                          AND is_default = TRUE
                        """,
                new MapSqlParameterSource()
                        .addValue("userId", userId)
                        .addValue("now", Timestamp.from(occurredAt))
        );
    }

    private UpdateUserAddressResult loadAddress(UUID addressId, UUID userId) {
        String sql = """
                SELECT id, user_id, receiver_name, phone, province_code, district_code,
                       ward_code, address_detail, is_default, created_at, updated_at
                FROM user_addresses
                WHERE id = :addressId
                  AND user_id = :userId
                """;
        List<UpdateUserAddressResult> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("addressId", addressId)
                        .addValue("userId", userId),
                this::mapResult
        );
        if (rows.isEmpty()) {
            throw new AppException(ErrorCode.ADDRESS_NOT_FOUND);
        }
        return rows.getFirst();
    }

    private UpdateUserAddressSnapshot mapSnapshot(ResultSet rs, int rowNum) throws SQLException {
        return new UpdateUserAddressSnapshot(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("user_id")),
                rs.getString("receiver_name"),
                rs.getString("phone"),
                rs.getString("province_code"),
                rs.getString("district_code"),
                rs.getString("ward_code"),
                rs.getString("address_detail"),
                rs.getBoolean("is_default")
        );
    }

    private UpdateUserAddressResult mapResult(ResultSet rs, int rowNum) throws SQLException {
        return new UpdateUserAddressResult(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("user_id")),
                rs.getString("receiver_name"),
                rs.getString("phone"),
                rs.getString("province_code"),
                rs.getString("district_code"),
                rs.getString("ward_code"),
                rs.getString("address_detail"),
                rs.getBoolean("is_default"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }
}
