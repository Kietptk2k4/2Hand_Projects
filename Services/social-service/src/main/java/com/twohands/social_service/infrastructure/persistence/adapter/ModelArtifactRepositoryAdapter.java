package com.twohands.social_service.infrastructure.persistence.adapter;

import com.twohands.social_service.domain.post.ModelArtifactRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ModelArtifactRepositoryAdapter implements ModelArtifactRepository {

    private static final String FIND_ACTIVE = """
            SELECT model_name, version, format, artifact_path
            FROM model_artifacts
            WHERE model_name = :modelName
              AND is_active = TRUE
            LIMIT 1
            """;

    private static final String LIST_BY_MODEL_NAME = """
            SELECT model_name, version, format, artifact_path, is_active, trained_at, metrics::text AS metrics
            FROM model_artifacts
            WHERE model_name = :modelName
            ORDER BY version DESC
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ModelArtifactRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ActiveModelArtifact> findActive(String modelName) {
        if (modelName == null || modelName.isBlank()) {
            return Optional.empty();
        }
        try {
            ActiveModelArtifact artifact = jdbcTemplate.queryForObject(
                    FIND_ACTIVE,
                    new MapSqlParameterSource("modelName", modelName),
                    (rs, rowNum) -> new ActiveModelArtifact(
                            rs.getString("model_name"),
                            rs.getInt("version"),
                            rs.getString("format"),
                            rs.getString("artifact_path")
                    )
            );
            return Optional.ofNullable(artifact);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    @Override
    public List<ModelArtifactListItem> listByModelName(String modelName) {
        if (modelName == null || modelName.isBlank()) {
            return List.of();
        }
        return jdbcTemplate.query(
                LIST_BY_MODEL_NAME,
                new MapSqlParameterSource("modelName", modelName),
                (rs, rowNum) -> new ModelArtifactListItem(
                        rs.getString("model_name"),
                        rs.getInt("version"),
                        rs.getString("format"),
                        rs.getString("artifact_path"),
                        rs.getBoolean("is_active"),
                        rs.getTimestamp("trained_at").toInstant(),
                        rs.getString("metrics")
                )
        );
    }
}
