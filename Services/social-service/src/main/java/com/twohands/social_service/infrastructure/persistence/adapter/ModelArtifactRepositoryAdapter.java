package com.twohands.social_service.infrastructure.persistence.adapter;

import com.twohands.social_service.domain.post.ModelArtifactRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

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
}
