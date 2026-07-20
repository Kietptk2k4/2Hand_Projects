package com.twohands.social_service.delivery.http.feed;

import com.twohands.social_service.application.feed.recommendposts.RecommendPostsUseCase;
import com.twohands.social_service.application.feed.viewfollowingfeed.ViewFollowingFeedUseCase;
import com.twohands.social_service.application.feed.viewglobalfeed.ViewGlobalFeedResult;
import com.twohands.social_service.application.feed.viewglobalfeed.ViewGlobalFeedUseCase;
import com.twohands.social_service.common.dto.ApiResponse;
import com.twohands.social_service.delivery.http.feed.mapper.ViewGlobalFeedHttpMapper;
import com.twohands.social_service.delivery.http.feed.response.ViewGlobalFeedResponse;
import com.twohands.social_service.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/social/feed")
public class FeedController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    private final ViewGlobalFeedUseCase viewGlobalFeedUseCase;
    private final ViewFollowingFeedUseCase viewFollowingFeedUseCase;
    private final RecommendPostsUseCase recommendPostsUseCase;
    private final ViewGlobalFeedHttpMapper viewGlobalFeedHttpMapper;

    public FeedController(
            ViewGlobalFeedUseCase viewGlobalFeedUseCase,
            ViewFollowingFeedUseCase viewFollowingFeedUseCase,
            RecommendPostsUseCase recommendPostsUseCase,
            ViewGlobalFeedHttpMapper viewGlobalFeedHttpMapper
    ) {
        this.viewGlobalFeedUseCase = viewGlobalFeedUseCase;
        this.viewFollowingFeedUseCase = viewFollowingFeedUseCase;
        this.recommendPostsUseCase = recommendPostsUseCase;
        this.viewGlobalFeedHttpMapper = viewGlobalFeedHttpMapper;
    }

    @GetMapping("/global")
    public ResponseEntity<ApiResponse<ViewGlobalFeedResponse>> viewGlobalFeed(
            @RequestParam(name = "page", defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(name = "size", defaultValue = "" + DEFAULT_SIZE) int size,
            Authentication authentication
    ) {
        UUID userId = resolveUserId(authentication);
        ViewGlobalFeedResult result = viewGlobalFeedUseCase.execute(userId, page, size);
        ViewGlobalFeedResponse response = viewGlobalFeedHttpMapper.toResponse(result);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        viewGlobalFeedUseCase.successMessage(),
                        response
                ));
    }

    @GetMapping("/following")
    public ResponseEntity<ApiResponse<ViewGlobalFeedResponse>> viewFollowingFeed(
            @RequestParam(name = "page", defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(name = "size", defaultValue = "" + DEFAULT_SIZE) int size,
            Authentication authentication
    ) {
        UUID userId = resolveUserId(authentication);
        ViewGlobalFeedResult result = viewFollowingFeedUseCase.execute(userId, page, size);
        ViewGlobalFeedResponse response = viewGlobalFeedHttpMapper.toResponse(result);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        viewFollowingFeedUseCase.successMessage(),
                        response
                ));
    }

    @GetMapping("/for-you")
    public ResponseEntity<ApiResponse<ViewGlobalFeedResponse>> viewRecommendFeed(
            @RequestParam(name = "page", defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(name = "size", defaultValue = "" + DEFAULT_SIZE) int size,
            Authentication authentication
    ) {
        UUID userId = resolveUserId(authentication);
        ViewGlobalFeedResult result = recommendPostsUseCase.execute(userId, page, size);
        ViewGlobalFeedResponse response = viewGlobalFeedHttpMapper.toResponse(result);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        recommendPostsUseCase.successMessage(),
                        response
                ));
    }

    private UUID resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            return null;
        }
        return principal.userId();
    }
}
