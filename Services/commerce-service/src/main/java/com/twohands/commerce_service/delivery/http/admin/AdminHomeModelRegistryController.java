package com.twohands.commerce_service.delivery.http.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.application.home.viewmodelartifacts.ViewCommerceHomeModelArtifactsUseCase;
import com.twohands.commerce_service.application.home.viewmodelstatus.ViewCommerceHomeModelStatusUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.delivery.http.admin.response.RecommendationModelArtifactResponse;
import com.twohands.commerce_service.delivery.http.admin.response.RecommendationModelStatusResponse;
import com.twohands.commerce_service.domain.home.HomeModelArtifactRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/commerce/api/v1/admin/home")
public class AdminHomeModelRegistryController {

    private final ObjectMapper objectMapper;
    private final ViewCommerceHomeModelArtifactsUseCase viewCommerceHomeModelArtifactsUseCase;
    private final ViewCommerceHomeModelStatusUseCase viewCommerceHomeModelStatusUseCase;

    public AdminHomeModelRegistryController(
            ObjectMapper objectMapper,
            ViewCommerceHomeModelArtifactsUseCase viewCommerceHomeModelArtifactsUseCase,
            ViewCommerceHomeModelStatusUseCase viewCommerceHomeModelStatusUseCase
    ) {
        this.objectMapper = objectMapper;
        this.viewCommerceHomeModelArtifactsUseCase = viewCommerceHomeModelArtifactsUseCase;
        this.viewCommerceHomeModelStatusUseCase = viewCommerceHomeModelStatusUseCase;
    }

    @GetMapping("/recommendation-model-artifacts")
    public ResponseEntity<ApiResponse<List<RecommendationModelArtifactResponse>>> viewArtifacts(
            Authentication authentication,
            @RequestParam(required = false) String modelName
    ) {
        List<RecommendationModelArtifactResponse> items = viewCommerceHomeModelArtifactsUseCase
                .execute(resolveActor(authentication), modelName)
                .stream()
                .map(this::toArtifactResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewCommerceHomeModelArtifactsUseCase.successMessage(),
                items
        ));
    }

    @GetMapping("/recommendation-model-status")
    public ResponseEntity<ApiResponse<RecommendationModelStatusResponse>> viewStatus(Authentication authentication) {
        ViewCommerceHomeModelStatusUseCase.CommerceHomeModelStatus status =
                viewCommerceHomeModelStatusUseCase.execute(resolveActor(authentication));
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewCommerceHomeModelStatusUseCase.successMessage(),
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
            HomeModelArtifactRepository.ModelArtifactListItem item
    ) {
        return new RecommendationModelArtifactResponse(
                item.modelName(),
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
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Khong the doc Commerce Home artifact metrics.", ex);
        }
    }
}
