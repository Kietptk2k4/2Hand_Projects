package com.twohands.commerce_service.domain.home;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface HomeModelArtifactRepository {

    Optional<ActiveModelArtifact> findActive(String modelName);

    List<ModelArtifactListItem> listByModelName(String modelName);

    record ActiveModelArtifact(String modelName, int version, String format, String artifactPath) {
    }

    record ModelArtifactListItem(
            String modelName,
            int version,
            String format,
            String artifactPath,
            boolean isActive,
            Instant trainedAt,
            String metricsJson
    ) {
    }
}
