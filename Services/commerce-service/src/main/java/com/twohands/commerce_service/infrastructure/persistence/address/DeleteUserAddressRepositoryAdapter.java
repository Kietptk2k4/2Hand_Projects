package com.twohands.commerce_service.infrastructure.persistence.address;

import com.twohands.commerce_service.domain.address.DeleteUserAddressRepository;
import com.twohands.commerce_service.domain.address.DeleteUserAddressResult;
import com.twohands.commerce_service.domain.address.OwnedUserAddress;
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
public class DeleteUserAddressRepositoryAdapter implements DeleteUserAddressRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public DeleteUserAddressRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<OwnedUserAddress> findOwnedAddress(UUID addressId, UUID userId) {
        String sql = """
                SELECT id, user_id, is_default
                FROM user_addresses
                WHERE id = :addressId
                  AND user_id = :userId
                """;
        List<OwnedUserAddress> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("addressId", addressId)
                        .addValue("userId", userId),
                this::mapOwnedAddress
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public DeleteUserAddressResult delete(OwnedUserAddress address, Instant occurredAt) {
        try {
            int deleted = jdbcTemplate.update(
                    "DELETE FROM user_addresses WHERE id = :addressId AND user_id = :userId",
                    new MapSqlParameterSource()
                            .addValue("addressId", address.addressId())
                            .addValue("userId", address.userId())
            );
            if (deleted == 0) {
                throw new AppException(ErrorCode.ADDRESS_NOT_FOUND);
            }

            UUID newDefaultAddressId = null;
            if (address.isDefault()) {
                newDefaultAddressId = promoteOldestRemainingDefault(address.userId(), occurredAt);
            }

            return new DeleteUserAddressResult(
                    address.addressId(),
                    address.userId(),
                    address.isDefault(),
                    newDefaultAddressId,
                    occurredAt
            );
        } catch (DataIntegrityViolationException ex) {
            throw new AppException(
                    ErrorCode.ADDRESS_DEFAULT_CONFLICT,
                    "Default address conflict while reassigning default",
                    ex
            );
        }
    }

    private UUID promoteOldestRemainingDefault(UUID userId, Instant occurredAt) {
        String selectSql = """
                SELECT id
                FROM user_addresses
                WHERE user_id = :userId
                ORDER BY created_at ASC
                LIMIT 1
                """;
        List<UUID> candidates = jdbcTemplate.query(
                selectSql,
                new MapSqlParameterSource("userId", userId),
                (rs, rowNum) -> UUID.fromString(rs.getString("id"))
        );
        if (candidates.isEmpty()) {
            return null;
        }

        UUID nextDefaultId = candidates.getFirst();
        String updateSql = """
                UPDATE user_addresses
                SET is_default = TRUE,
                    updated_at = :now
                WHERE id = :addressId
                  AND user_id = :userId
                """;
        jdbcTemplate.update(updateSql, new MapSqlParameterSource()
                .addValue("addressId", nextDefaultId)
                .addValue("userId", userId)
                .addValue("now", Timestamp.from(occurredAt)));
        return nextDefaultId;
    }

    private OwnedUserAddress mapOwnedAddress(ResultSet rs, int rowNum) throws SQLException {
        return new OwnedUserAddress(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("user_id")),
                rs.getBoolean("is_default")
        );
    }
}
