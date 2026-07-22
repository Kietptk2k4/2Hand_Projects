package com.twohands.social_service.delivery.http.admin;

import com.twohands.social_service.application.admin.viewrecommendationmodelstatus.ViewRecommendationModelStatusUseCase;
import com.twohands.social_service.common.dto.ApiResponse;
import com.twohands.social_service.delivery.http.admin.response.RecommendationModelStatusResponse;
import com.twohands.social_service.domain.post.RankingModelRuntimeStatus;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import com.twohands.social_service.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/social/admin")
public class SocialAdminRecommendationController {

    private final ViewRecommendationModelStatusUseCase viewRecommendationModelStatusUseCase;

    public SocialAdminRecommendationController(
            ViewRecommendationModelStatusUseCase viewRecommendationModelStatusUseCase
    ) {
        this.viewRecommendationModelStatusUseCase = viewRecommendationModelStatusUseCase;
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
}
