package com.twohands.commerce_service.delivery.http.home;

import com.twohands.commerce_service.application.home.recommendproducts.RecommendCommerceHomeUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.domain.home.HomeRecommendationResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/home")
public class HomeRecommendationController {

    private final RecommendCommerceHomeUseCase recommendCommerceHomeUseCase;

    public HomeRecommendationController(RecommendCommerceHomeUseCase recommendCommerceHomeUseCase) {
        this.recommendCommerceHomeUseCase = recommendCommerceHomeUseCase;
    }

    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<HomeRecommendationResponse>> recommend(Authentication authentication) {
        UUID userId = resolveUserId(authentication);
        HomeRecommendationResult result = recommendCommerceHomeUseCase.execute(userId);
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                recommendCommerceHomeUseCase.successMessage(),
                toResponse(result)
        ));
    }

    private UUID resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return principal.userId();
    }

    private HomeRecommendationResponse toResponse(HomeRecommendationResult result) {
        return new HomeRecommendationResponse(
                result.requestId(),
                result.rankingMode().name(),
                result.modelName(),
                result.modelVersion(),
                result.items().stream()
                        .map(item -> new HomeRecommendationResponse.HomeRecommendationItemResponse(
                                item.id(),
                                item.title(),
                                item.price(),
                                item.thumbnail(),
                                new HomeRecommendationResponse.ShopSummaryResponse(item.shop().id(), item.shop().name()),
                                item.rating() == null
                                        ? null
                                        : new HomeRecommendationResponse.RatingSummaryResponse(
                                                item.rating().avg(),
                                                item.rating().count()
                                        )
                        ))
                        .toList()
        );
    }
}
