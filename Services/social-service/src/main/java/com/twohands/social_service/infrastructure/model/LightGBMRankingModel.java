package com.twohands.social_service.infrastructure.model;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtSession;
import com.twohands.social_service.domain.post.PostFeatureVector;
import com.twohands.social_service.domain.post.RankingModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component("lightGbmRankingModel")
public class LightGBMRankingModel implements RankingModel {

    private static final Logger log = LoggerFactory.getLogger(LightGBMRankingModel.class);

    private final ModelLoader modelLoader;

    public LightGBMRankingModel(ModelLoader modelLoader) {
        this.modelLoader = modelLoader;
    }

    @Override
    public double predict(PostFeatureVector features) {
        if (features == null) {
            return 0.0;
        }
        List<Double> results = predictBatch(List.of(features));
        return results.isEmpty() ? 0.0 : results.getFirst();
    }

    @Override
    public List<Double> predictBatch(List<PostFeatureVector> featuresList) {
        if (featuresList == null || featuresList.isEmpty()) {
            return List.of();
        }

        OrtSession session = modelLoader.getSession();
        if (session == null) {
            // Fallback to empty/default double scores if OrtSession is not loaded
            log.trace("ONNX OrtSession is not loaded. Skipping ONNX batch predict.");
            return featuresList.stream().map(f -> 0.0).toList();
        }

        try {
            int batchSize = featuresList.size();
            float[][] inputData = new float[batchSize][6];
            for (int i = 0; i < batchSize; i++) {
                PostFeatureVector features = featuresList.get(i);
                inputData[i][0] = (float) features.recencyScore();
                inputData[i][1] = (float) features.engagementScore();
                inputData[i][2] = (float) features.hashtagMatchScore();
                inputData[i][3] = (float) features.authorAffinityScore();
                inputData[i][4] = (float) features.mutualFollowScore();
                inputData[i][5] = (float) features.crossDomainProductScore();
            }

            OnnxTensor inputTensor = OnnxTensor.createTensor(modelLoader.getEnv(), inputData);
            String inputName = session.getInputNames().iterator().next();

            try (OrtSession.Result results = session.run(Map.of(inputName, inputTensor))) {
                Object value = resolveScoreOutput(results);
                List<Double> scores = new ArrayList<>(batchSize);

                if (value instanceof float[][] output2d) {
                    for (int i = 0; i < batchSize; i++) {
                        float[] row = output2d[i];
                        // Binary classifier probabilities: use positive class (index 1) when present.
                        float score = row.length >= 2 ? row[1] : row[0];
                        scores.add((double) score);
                    }
                } else if (value instanceof float[] output1d) {
                    for (int i = 0; i < batchSize; i++) {
                        scores.add((double) output1d[i]);
                    }
                } else {
                    log.warn("Unexpected ONNX output value type: {}", value.getClass().getName());
                    return featuresList.stream().map(f -> 0.0).toList();
                }

                return scores;
            } finally {
                inputTensor.close();
            }
        } catch (Exception e) {
            log.error("Failed to perform ONNX model predictBatch, falling back to 0.0 scores.", e);
            return featuresList.stream().map(f -> 0.0).toList();
        }
    }

    /**
     * Prefer probability tensor (LightGBM classifier ONNX) over label tensor.
     */
    private static Object resolveScoreOutput(OrtSession.Result results) throws Exception {
        if (results.size() >= 2) {
            return results.get(1).getValue();
        }
        return results.get(0).getValue();
    }
}
