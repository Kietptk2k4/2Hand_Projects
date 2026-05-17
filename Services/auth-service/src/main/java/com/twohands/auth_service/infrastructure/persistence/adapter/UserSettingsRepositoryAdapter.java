package com.twohands.auth_service.infrastructure.persistence.adapter;

import com.twohands.auth_service.domain.user.UserSettings;
import com.twohands.auth_service.domain.user.UserSettingsRepository;
import com.twohands.auth_service.infrastructure.persistence.mapper.UserJdbcMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UserSettingsRepositoryAdapter implements UserSettingsRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final UserJdbcMapper jdbcMapper;

    public UserSettingsRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate, UserJdbcMapper jdbcMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcMapper = jdbcMapper;
    }

    @Override
    public Optional<UserSettings> findByUserId(UUID userId) {
        String sql = """
                SELECT user_id, appearance_mode, created_at, updated_at
                FROM user_settings
                WHERE user_id = :userId
                """;

        return jdbcTemplate.query(sql, new MapSqlParameterSource("userId", userId), (rs, rowNum) -> jdbcMapper.toUserSettings(rs))
                .stream()
                .findFirst();
    }

    @Override
    public UserSettings save(UserSettings settings) {
        String sql = """
                INSERT INTO user_settings(user_id, appearance_mode, created_at, updated_at)
                VALUES (:userId, :appearanceMode, :createdAt, :updatedAt)
                """;

        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("userId", settings.userId())
                .addValue("appearanceMode", settings.appearanceMode().name())
                .addValue("createdAt", settings.createdAt())
                .addValue("updatedAt", settings.updatedAt()));

        return settings;
    }

    @Override
    public int updateByUserId(UserSettings settings) {
        String sql = """
                UPDATE user_settings
                SET appearance_mode = :appearanceMode,
                    updated_at = :updatedAt
                WHERE user_id = :userId
                """;

        return jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("userId", settings.userId())
                .addValue("appearanceMode", settings.appearanceMode().name())
                .addValue("updatedAt", settings.updatedAt()));
    }

    @Override
    public void deleteByUserId(UUID userId) {
        jdbcTemplate.update("DELETE FROM user_settings WHERE user_id = :userId", new MapSqlParameterSource("userId", userId));
    }
}
