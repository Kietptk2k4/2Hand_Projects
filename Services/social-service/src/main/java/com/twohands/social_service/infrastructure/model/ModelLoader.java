package com.twohands.social_service.infrastructure.model;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
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

    @Value("${social.recommendation.model-path}")
    private String modelPath;

    private volatile OrtSession session;
    private final OrtEnvironment env = OrtEnvironment.getEnvironment();

    @PostConstruct
    public void init() {
        loadModelInternal();
    }

    @Scheduled(cron = "${social.recommendation.reload-cron}")
    public void reloadModel() {
        log.info("Scheduled reload checking for recommendation model at: {}", modelPath);
        loadModelInternal();
    }

    public synchronized void forceReload() {
        log.info("Force reloading recommendation model from: {}", modelPath);
        loadModelInternal();
    }

    private synchronized void loadModelInternal() {
        File file = new File(modelPath);
        if (!file.exists()) {
            log.warn("Recommendation model file not found at: {}. Using fallback RuleBasedRankingModel.", modelPath);
            OrtSession oldSession = this.session;
            this.session = null;
            if (oldSession != null) {
                try {
                    oldSession.close();
                } catch (Exception ignored) {}
            }
            return;
        }

        try {
            log.info("Loading ONNX recommendation model from: {}", modelPath);
            OrtSession newSession = env.createSession(modelPath);
            OrtSession oldSession = this.session;
            this.session = newSession;
            
            if (oldSession != null) {
                try {
                    oldSession.close();
                } catch (Exception e) {
                    log.error("Failed to close old OrtSession during model reload", e);
                }
            }
            log.info("ONNX recommendation model loaded successfully.");
        } catch (Exception e) {
            log.error("Failed to load ONNX model. Using fallback RuleBasedRankingModel.", e);
            // On load error, we do not overwrite active session if it exists to preserve current serving,
            // but if there is no active session, it remains null (fallback active).
        }
    }

    public OrtSession getSession() {
        return this.session;
    }

    public OrtEnvironment getEnv() {
        return this.env;
    }
}
