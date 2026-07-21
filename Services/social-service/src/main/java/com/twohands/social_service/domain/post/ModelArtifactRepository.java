package com.twohands.social_service.domain.post;

import java.util.Optional;

public interface ModelArtifactRepository {

    Optional<ActiveModelArtifact> findActive(String modelName);

    record ActiveModelArtifact(String modelName, int version, String format, String artifactPath) {
    }
}
