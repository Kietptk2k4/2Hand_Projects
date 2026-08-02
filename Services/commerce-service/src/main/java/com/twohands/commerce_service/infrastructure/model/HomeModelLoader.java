package com.twohands.commerce_service.infrastructure.model;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.domain.home.HomeFeatureOrder;
import com.twohands.commerce_service.domain.home.HomeModelArtifactRepository;
import com.twohands.commerce_service.domain.home.PopularityNormalizer;
import com.twohands.commerce_service.domain.home.RankingMode;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class HomeModelLoader {

    private static final Logger log = LoggerFactory.getLogger(HomeModelLoader.class);
    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {
    };
    private static final String DEFAULT_MODEL_NAME = "commerce_home_ranker";

    public static final String REASON_FILE_NOT_FOUND = "file_not_found";
    public static final String REASON_LOAD_ERROR = "load_error";
    public static final String REASON_ONNX_SESSION_MISSING = "onnx_session_missing";
    public static final String REASON_FEATURE_ORDER_MISMATCH = "feature_order_mismatch";
    public static final String REASON_NORMALIZER_MISSING = "normalizer_missing";

    @Value("${commerce.home.recommendation.model-path:commerce_home_ranker.onnx}")
    private String modelPath;

    @Value("${commerce.home.recommendation.model-root:}")
    private String modelRoot;

    @Value("${commerce.home.recommendation.model-name:commerce_home_ranker}")
    private String modelName;

    private final HomeModelArtifactRepository modelArtifactRepository;
    private final ObjectMapper objectMapper;
    private final OrtEnvironment env = OrtEnvironment.getEnvironment();

    private volatile OrtSession session;
    private volatile Integer activeModelVersion;
    private volatile String fallbackReason = REASON_ONNX_SESSION_MISSING;
    private volatile boolean featureOrderValid;
    private volatile PopularityNormalizer popularityNormalizer;

    public HomeModelLoader(HomeModelArtifactRepository modelArtifactRepository, ObjectMapper objectMapper) {
        this.modelArtifactRepository = modelArtifactRepository;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        loadModelInternal();
    }

    @Scheduled(cron = "${commerce.home.recommendation.reload-cron:0 */5 * * * *}")
    public void reloadModel() {
        loadModelInternal();
    }

    public synchronized void forceReload() {
        loadModelInternal();
    }

    public HomeModelRuntime resolveRuntime() {
        if (session != null && featureOrderValid && popularityNormalizer != null) {
            return new HomeModelRuntime(
                    RankingMode.LIGHTGBM,
                    getModelName(),
                    activeModelVersion,
                    null,
                    popularityNormalizer
            );
        }
        return new HomeModelRuntime(
                RankingMode.DEGRADED,
                null,
                null,
                fallbackReason == null ? REASON_ONNX_SESSION_MISSING : fallbackReason,
                popularityNormalizer == null ? new PopularityNormalizer(0.0, 1.0) : popularityNormalizer
        );
    }

    public List<Double> scoreBatch(List<double[]> featureVectors) {
        if (featureVectors == null || featureVectors.isEmpty() || session == null) {
            return List.of();
        }
        try {
            float[][] input = new float[featureVectors.size()][HomeFeatureOrder.DIM];
            for (int row = 0; row < featureVectors.size(); row++) {
                for (int col = 0; col < HomeFeatureOrder.DIM; col++) {
                    input[row][col] = (float) featureVectors.get(row)[col];
                }
            }
            OnnxTensor inputTensor = OnnxTensor.createTensor(env, input);
            String inputName = session.getInputNames().iterator().next();
            try (OrtSession.Result results = session.run(Map.of(inputName, inputTensor))) {
                Object value = resolveScoreOutput(results);
                List<Double> scores = new ArrayList<>(featureVectors.size());
                if (value instanceof float[][] output2d) {
                    for (float[] row : output2d) {
                        scores.add((double) (row.length >= 2 ? row[1] : row[0]));
                    }
                    return scores;
                }
                if (value instanceof float[] output1d) {
                    for (float score : output1d) {
                        scores.add((double) score);
                    }
                    return scores;
                }
                log.warn("Unexpected Home ONNX output type: {}", value.getClass().getName());
            } finally {
                inputTensor.close();
            }
        } catch (Exception ex) {
            log.warn("Failed scoring Commerce Home candidates with ONNX", ex);
        }
        return featureVectors.stream().map(vector -> 0.0).toList();
    }

    public static boolean isSafeArtifactBasename(String artifactPath) {
        if (artifactPath == null) {
            return false;
        }
        String trimmed = artifactPath.trim();
        if (trimmed.isEmpty() || trimmed.contains("/") || trimmed.contains("\\") || trimmed.contains("..")) {
            return false;
        }
        return !Paths.get(trimmed).isAbsolute();
    }

    public static Path resolveUnderModelRoot(String modelRoot, String artifactBasename) {
        if (modelRoot == null || modelRoot.isBlank() || !isSafeArtifactBasename(artifactBasename)) {
            return null;
        }
        Path root = Paths.get(modelRoot.trim()).toAbsolutePath().normalize();
        Path resolved = root.resolve(artifactBasename.trim()).normalize();
        return resolved.startsWith(root) ? resolved : null;
    }

    private synchronized void loadModelInternal() {
        String resolvedPath = modelPath;
        Integer resolvedVersion = null;
        try {
            var active = modelArtifactRepository.findActive(getModelName());
            if (active.isPresent()) {
                HomeModelArtifactRepository.ActiveModelArtifact artifact = active.get();
                Path underRoot = resolveUnderModelRoot(modelRoot, artifact.artifactPath());
                if (underRoot != null && underRoot.toFile().exists()) {
                    resolvedPath = underRoot.toString();
                    resolvedVersion = artifact.version();
                }
            }
        } catch (Exception ex) {
            log.warn("Could not resolve active Commerce Home artifact; using configured model path", ex);
        }

        File file = new File(resolvedPath);
        if (!file.exists()) {
            clearSession(REASON_FILE_NOT_FOUND);
            return;
        }

        try {
            OrtSession newSession = env.createSession(resolvedPath);
            OrtSession oldSession = this.session;
            this.session = newSession;
            this.activeModelVersion = resolvedVersion;
            this.featureOrderValid = loadFeatureOrderSidecar(file.toPath());
            this.popularityNormalizer = loadNormalizerSidecar(file.toPath());
            this.fallbackReason = resolveFallbackReason();
            if (oldSession != null) {
                oldSession.close();
            }
        } catch (Exception ex) {
            log.warn("Failed to load Commerce Home ONNX model", ex);
            clearSession(REASON_LOAD_ERROR);
        }
    }

    private boolean loadFeatureOrderSidecar(Path onnxPath) {
        Path sidecar = siblingSidecar(onnxPath, ".feature_order.json");
        if (!Files.exists(sidecar)) {
            return false;
        }
        try {
            List<String> exported = objectMapper.readValue(sidecar.toFile(), STRING_LIST);
            return HomeFeatureOrder.matches(exported);
        } catch (Exception ex) {
            log.warn("Failed to read feature order sidecar {}", sidecar, ex);
            return false;
        }
    }

    private PopularityNormalizer loadNormalizerSidecar(Path onnxPath) {
        Path sidecar = siblingSidecar(onnxPath, ".popularity_normalizer.json");
        if (!Files.exists(sidecar)) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(sidecar.toFile());
            return new PopularityNormalizer(root.path("z_lo").asDouble(), root.path("z_hi").asDouble());
        } catch (Exception ex) {
            log.warn("Failed to read popularity normalizer sidecar {}", sidecar, ex);
            return null;
        }
    }

    private Path siblingSidecar(Path onnxPath, String suffix) {
        String fileName = onnxPath.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        String baseName = dotIndex >= 0 ? fileName.substring(0, dotIndex) : fileName;
        return onnxPath.resolveSibling(baseName + suffix);
    }

    private String resolveFallbackReason() {
        if (session == null) {
            return REASON_ONNX_SESSION_MISSING;
        }
        if (!featureOrderValid) {
            return REASON_FEATURE_ORDER_MISMATCH;
        }
        if (popularityNormalizer == null) {
            return REASON_NORMALIZER_MISSING;
        }
        return null;
    }

    private void clearSession(String reason) {
        OrtSession oldSession = this.session;
        this.session = null;
        this.activeModelVersion = null;
        this.featureOrderValid = false;
        this.popularityNormalizer = null;
        this.fallbackReason = reason;
        if (oldSession != null) {
            try {
                oldSession.close();
            } catch (Exception ignored) {
            }
        }
    }

    private String getModelName() {
        return modelName == null || modelName.isBlank() ? DEFAULT_MODEL_NAME : modelName.trim();
    }

    private static Object resolveScoreOutput(OrtSession.Result results) throws Exception {
        if (results.size() >= 2) {
            return results.get(1).getValue();
        }
        return results.get(0).getValue();
    }

    public record HomeModelRuntime(
            RankingMode rankingMode,
            String modelName,
            Integer modelVersion,
            String fallbackReason,
            PopularityNormalizer normalizer
    ) {
    }
}
