package com.twohands.social_service.infrastructure.model;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import com.twohands.social_service.domain.post.ModelArtifactRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class ModelLoader {

    private static final Logger log = LoggerFactory.getLogger(ModelLoader.class);
    private static final String DEFAULT_MODEL_NAME = "feed_ranker";

    @Value("${social.recommendation.model-path}")
    private String modelPath;

    @Value("${social.recommendation.model-name:feed_ranker}")
    private String modelName;

    private volatile OrtSession session;
    private volatile Integer activeModelVersion;
    private final OrtEnvironment env = OrtEnvironment.getEnvironment();
    private final ModelArtifactRepository modelArtifactRepository;

    public ModelLoader(ModelArtifactRepository modelArtifactRepository) {
        this.modelArtifactRepository = modelArtifactRepository;
    }

    @PostConstruct
    public void init() {
        loadModelInternal();
    }

    @Scheduled(cron = "${social.recommendation.reload-cron}")
    public void reloadModel() {
        log.info("Scheduled reload checking for recommendation model");
        loadModelInternal();
    }

    public synchronized void forceReload() {
        log.info("Force reloading recommendation model");
        loadModelInternal();
    }

    private synchronized void loadModelInternal() {
        String resolvedPath = modelPath;
        Integer resolvedVersion = null;

        try {
            var active = modelArtifactRepository.findActive(
                    modelName != null && !modelName.isBlank() ? modelName : DEFAULT_MODEL_NAME
            );
            if (active.isPresent()) {
                ModelArtifactRepository.ActiveModelArtifact artifact = active.get();
                File artifactFile = new File(artifact.artifactPath());
                if (artifactFile.exists()) {
                    resolvedPath = artifact.artifactPath();
                    resolvedVersion = artifact.version();
                    log.info(
                            "Resolved active model artifact {} v{} at {}",
                            artifact.modelName(),
                            artifact.version(),
                            resolvedPath
                    );
                } else {
                    log.warn(
                            "Active model artifact path missing: {}. Falling back to configured model-path {}",
                            artifact.artifactPath(),
                            modelPath
                    );
                }
            }
        } catch (Exception ex) {
            log.warn("Could not resolve active model_artifacts row; using configured model-path", ex);
        }

        File file = new File(resolvedPath);
        if (!file.exists()) {
            log.warn("Recommendation model file not found at: {}. Using fallback RuleBasedRankingModel.", resolvedPath);
            OrtSession oldSession = this.session;
            this.session = null;
            this.activeModelVersion = null;
            if (oldSession != null) {
                try {
                    oldSession.close();
                } catch (Exception ignored) {
                }
            }
            return;
        }

        try {
            log.info("Loading ONNX recommendation model from: {}", resolvedPath);
            OrtSession newSession = env.createSession(resolvedPath);
            OrtSession oldSession = this.session;
            this.session = newSession;
            this.activeModelVersion = resolvedVersion;

            if (oldSession != null) {
                try {
                    oldSession.close();
                } catch (Exception e) {
                    log.error("Failed to close old OrtSession during model reload", e);
                }
            }
            log.info("ONNX recommendation model loaded successfully. version={}", resolvedVersion);
        } catch (Exception e) {
            log.error("Failed to load ONNX model. Using fallback RuleBasedRankingModel.", e);
        }
    }

    public OrtSession getSession() {
        return this.session;
    }

    public OrtEnvironment getEnv() {
        return this.env;
    }

    /**
     * Active registry version, or null when rule-based fallback / unknown file-only load.
     */
    public Integer getActiveModelVersion() {
        return session == null ? null : activeModelVersion;
    }

    public String getModelName() {
        return modelName != null && !modelName.isBlank() ? modelName : DEFAULT_MODEL_NAME;
    }
}
