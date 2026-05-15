package com.twohands.auth_service.infrastructure.persistence.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.auth_service.domain.user.AppearanceMode;
import com.twohands.auth_service.domain.user.EmailAddress;
import com.twohands.auth_service.domain.user.PasswordHash;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserProfile;
import com.twohands.auth_service.domain.user.UserSettings;
import com.twohands.auth_service.domain.user.UserStatus;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class UserJdbcMapper {

    private final ObjectMapper objectMapper;

    public UserJdbcMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public User toUser(ResultSet rs) throws SQLException {
        return User.rehydrate(
                UUID.fromString(rs.getString("id")),
                EmailAddress.of(rs.getString("email")),
                PasswordHash.of(rs.getString("password_hash")),
                UserStatus.valueOf(rs.getString("status")),
                rs.getBoolean("email_verified"),
                rs.getBoolean("phone_verified"),
                toInstantNullable(rs, "last_login_at"),
                toInstantNullable(rs, "password_changed_at"),
                toInstantNullable(rs, "deleted_at"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }

    public UserProfile toUserProfile(ResultSet rs) throws SQLException {
        return UserProfile.rehydrate(
                UUID.fromString(rs.getString("user_id")),
                rs.getString("display_name"),
                rs.getString("avatar_url"),
                rs.getString("bio"),
                rs.getString("website"),
                parseSocialLinks(rs.getString("social_links")),
                rs.getBoolean("is_private"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }

    public UserSettings toUserSettings(ResultSet rs) throws SQLException {
        return UserSettings.rehydrate(
                UUID.fromString(rs.getString("user_id")),
                AppearanceMode.valueOf(rs.getString("appearance_mode")),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }

    private Instant toInstantNullable(ResultSet rs, String column) throws SQLException {
        var timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toInstant();
    }

    private Map<String, String> parseSocialLinks(String value) {
        if (value == null || value.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<>() {});
        } catch (Exception ex) {
            return Map.of();
        }
    }
}
