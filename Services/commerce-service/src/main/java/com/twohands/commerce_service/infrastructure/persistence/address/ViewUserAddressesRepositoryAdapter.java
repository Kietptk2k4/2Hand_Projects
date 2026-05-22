package com.twohands.commerce_service.infrastructure.persistence.address;

import com.twohands.commerce_service.domain.address.UserAddressListItem;
import com.twohands.commerce_service.domain.address.ViewUserAddressesRepository;
import com.twohands.commerce_service.domain.address.ViewUserAddressesResult;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class ViewUserAddressesRepositoryAdapter implements ViewUserAddressesRepository {

    private static final String FIND_BY_USER_ID = """
            SELECT id,
                   receiver_name,
                   phone,
                   province_code,
                   district_code,
                   ward_code,
                   address_detail,
                   is_default,
                   created_at,
                   updated_at
            FROM user_addresses
            WHERE user_id = :userId
            ORDER BY is_default DESC, updated_at DESC, created_at DESC
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ViewUserAddressesRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ViewUserAddressesResult findByUserId(UUID userId) {
        List<UserAddressListItem> addresses = jdbcTemplate.query(
                FIND_BY_USER_ID,
                new MapSqlParameterSource("userId", userId),
                this::mapRow
        );
        return new ViewUserAddressesResult(addresses);
    }

    private UserAddressListItem mapRow(ResultSet rs, int rowNum) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        return new UserAddressListItem(
                UUID.fromString(rs.getString("id")),
                rs.getString("receiver_name"),
                rs.getString("phone"),
                rs.getString("province_code"),
                rs.getString("district_code"),
                rs.getString("ward_code"),
                rs.getString("address_detail"),
                rs.getBoolean("is_default"),
                createdAt == null ? null : createdAt.toInstant(),
                updatedAt == null ? null : updatedAt.toInstant()
        );
    }
}
