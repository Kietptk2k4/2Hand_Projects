package com.twohands.social_service.infrastructure.model;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import com.twohands.social_service.domain.post.ModelArtifactRepository;
import com.twohands.social_service.domain.post.RankingModelRuntimeStatus;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class ModelLoader {

    private static final Logger log = LoggerFactory.getLogger(ModelLoader.class);
    private static final String DEFAULT_MODEL_NAME = "feed_ranker";

    public static final String REASON_FILE_NOT_FOUND = "file_not_found";
    public static final String REASON_LOAD_ERROR = "load_error";
    public static final String REASON_ONNX_SESSION_MISSING = "onnx_session_missing";
    public static final String REASON_CONFIG_RULE_BASED = "config_rule_based";

    @Value("${social.recommendation.model-path}")
    private String modelPath;

    @Value("${social.recommendation.model-name:feed_ranker}")
    private String modelName;

    /**
     * Directory that contains ONNX files referenced by portable {@code model_artifacts.artifact_path}
     * basenames. Ops alias: {@code MODEL_ROOT}. Env: {@code SOCIAL_RECOMMENDATION_MODEL_ROOT}.
     */
    @Value("${social.recommendation.model-root:}")
    private String modelRoot;

    private volatile OrtSession session;
    private volatile Integer activeModelVersion;
    private volatile String fallbackReason = REASON_ONNX_SESSION_MISSING;
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

    /**
     * Phase 1: only a single path segment (basename) is allowed — no separators, {@code ..}, or absolutes.
     */
    public static boolean isSafeArtifactBasename(String artifactPath) {
        if (artifactPath == null) {
            return false;
        }
        String trimmed = artifactPath.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        if (trimmed.contains("/") || trimmed.contains("\\") || trimmed.contains("..")) {
            return false;
        }
        Path asPath = Paths.get(trimmed);
        return !asPath.isAbsolute();
    }

    /**
     * Join model root with a safe basename. Returns null if root blank, basename unsafe, or escapes root.
     */
    public static Path resolveUnderModelRoot(String modelRoot, String artifactBasename) {
        if (modelRoot == null || modelRoot.isBlank()) {
            return null;
        }
        if (!isSafeArtifactBasename(artifactBasename)) {
            return null;
        }
        Path root = Paths.get(modelRoot.trim()).toAbsolutePath().normalize();
        Path resolved = root.resolve(artifactBasename.trim()).normalize();
        if (!resolved.startsWith(root)) {
            return null;
        }
        return resolved;
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
                String stored = artifact.artifactPath();
                Path underRoot = resolveUnderModelRoot(modelRoot, stored);
                if (underRoot != null && underRoot.toFile().exists()) {
                    resolvedPath = underRoot.toString();
                    resolvedVersion = artifact.version();
                    log.info(
                            "Resolved active model artifact {} v{} at {}",
                            artifact.modelName(),
                            artifact.version(),
                            resolvedPath
                    );
                } else if (underRoot != null) {
                    log.warn(
                            "Active model artifact missing under model-root: {}. Falling back to configured model-path {}",
                            underRoot,
                            modelPath
                    );
                } else {
                    log.warn(
                            "Active model artifact_path is not a safe relative basename under model-root "
                                    + "(path='{}', modelRoot='{}'). Falling back to configured model-path {}",
                            stored,
                            modelRoot,
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
            clearSession(REASON_FILE_NOT_FOUND);
            return;
        }

        try {
            log.info("Loading ONNX recommendation model from: {}", resolvedPath);
            OrtSession newSession = env.createSession(resolvedPath);
            OrtSession oldSession = this.session;
            this.session = newSession;
            this.activeModelVersion = resolvedVersion;
            this.fallbackReason = null;

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
            clearSession(REASON_LOAD_ERROR);
        }
    }

    private void clearSession(String reason) {
        OrtSession oldSession = this.session;
        this.session = null;
        this.activeModelVersion = null;
        this.fallbackReason = reason;
        if (oldSession != null) {
            try {
                oldSession.close();
            } catch (Exception ignored) {
            }
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

    public String getFallbackReason() {
        return fallbackReason;
    }

    public RankingModelRuntimeStatus resolveRuntimeStatus(String configuredRankingModel) {
        String configured = configuredRankingModel == null || configuredRankingModel.isBlank()
                ? "lightgbm"
                : configuredRankingModel.trim();
        boolean wantsLightGbm = "lightgbm".equalsIgnoreCase(configured);
        if (wantsLightGbm && session != null) {
            return new RankingModelRuntimeStatus(
                    "lightgbm",
                    activeModelVersion,
                    getModelName(),
                    null,
                    configured
            );
        }
        String reason;
        if (!wantsLightGbm) {
            reason = REASON_CONFIG_RULE_BASED;
        } else if (fallbackReason != null && !fallbackReason.isBlank()) {
            reason = fallbackReason;
        } else {
            reason = REASON_ONNX_SESSION_MISSING;
        }
        return new RankingModelRuntimeStatus(
                "rule_based",
                null,
                null,
                reason,
                configured
        );
    }
}
