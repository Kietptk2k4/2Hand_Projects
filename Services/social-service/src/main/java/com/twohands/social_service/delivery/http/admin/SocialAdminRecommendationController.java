package com.twohands.social_service.delivery.http.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.social_service.application.admin.viewrecommendationmodelartifacts.ViewRecommendationModelArtifactsUseCase;
import com.twohands.social_service.application.admin.viewrecommendationmodelstatus.ViewRecommendationModelStatusUseCase;
import com.twohands.social_service.common.dto.ApiResponse;
import com.twohands.social_service.delivery.http.admin.response.RecommendationModelArtifactResponse;
import com.twohands.social_service.delivery.http.admin.response.RecommendationModelStatusResponse;
import com.twohands.social_service.domain.post.ModelArtifactRepository;
import com.twohands.social_service.domain.post.RankingModelRuntimeStatus;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import com.twohands.social_service.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/social/admin")
public class SocialAdminRecommendationController {

    private final ObjectMapper objectMapper;
    private final ViewRecommendationModelArtifactsUseCase viewRecommendationModelArtifactsUseCase;
    private final ViewRecommendationModelStatusUseCase viewRecommendationModelStatusUseCase;

    public SocialAdminRecommendationController(
            ObjectMapper objectMapper,
            ViewRecommendationModelArtifactsUseCase viewRecommendationModelArtifactsUseCase,
            ViewRecommendationModelStatusUseCase viewRecommendationModelStatusUseCase
    ) {
        this.objectMapper = objectMapper;
        this.viewRecommendationModelArtifactsUseCase = viewRecommendationModelArtifactsUseCase;
        this.viewRecommendationModelStatusUseCase = viewRecommendationModelStatusUseCase;
    }

    @GetMapping("/recommendation-model-artifacts")
    public ResponseEntity<ApiResponse<List<RecommendationModelArtifactResponse>>> viewArtifacts(
            Authentication authentication,
            @RequestParam(required = false) String modelName
    ) {
        List<RecommendationModelArtifactResponse> items = viewRecommendationModelArtifactsUseCase
                .execute(resolveActor(authentication), modelName)
                .stream()
                .map(this::toArtifactResponse)
                .toList();

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        viewRecommendationModelArtifactsUseCase.successMessage(),
                        items
                ));
    }

    @GetMapping("/recommendation-model-status")
    public ResponseEntity<ApiResponse<RecommendationModelStatusResponse>> viewStatus(
            Authentication authentication
    ) {
        RankingModelRuntimeStatus status = viewRecommendationModelStatusUseCase.execute(resolveActor(authentication));
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        viewRecommendationModelStatusUseCase.successMessage(),
                        new RecommendationModelStatusResponse(
                                status.mode(),
                                status.modelVersion(),
                                status.modelName(),
                                status.reason(),
                                status.configuredRankingModel()
                        )
                ));
    }

    private AuthenticatedUser resolveActor(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.defaultMessage());
        }
        return principal;
    }

    private RecommendationModelArtifactResponse toArtifactResponse(
            ModelArtifactRepository.ModelArtifactListItem item
    ) {
        return new RecommendationModelArtifactResponse(
                item.version(),
                item.format(),
                item.artifactPath(),
                item.isActive(),
                item.trainedAt(),
                parseMetrics(item.metricsJson())
        );
    }

    private Object parseMetrics(String metricsJson) {
        if (metricsJson == null || metricsJson.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(metricsJson, Object.class);
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Khong the doc metrics model artifact.", ex);
        }
    }
}
