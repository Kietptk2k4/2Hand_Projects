package com.twohands.social_service.delivery.http.user;

import com.twohands.social_service.application.user.followuser.FollowUserCommand;
import com.twohands.social_service.application.user.followuser.FollowUserResult;
import com.twohands.social_service.application.user.followuser.FollowUserUseCase;
import com.twohands.social_service.application.user.unfollowuser.UnfollowUserCommand;
import com.twohands.social_service.application.user.unfollowuser.UnfollowUserResult;
import com.twohands.social_service.application.user.unfollowuser.UnfollowUserUseCase;
import com.twohands.social_service.application.user.viewsocialprofile.ViewSocialProfileCommand;
import com.twohands.social_service.application.user.viewsocialprofile.ViewSocialProfileResult;
import com.twohands.social_service.application.user.viewsocialprofile.ViewSocialProfileUseCase;
import com.twohands.social_service.common.dto.ApiResponse;
import com.twohands.social_service.delivery.http.user.response.FollowUserResponse;
import com.twohands.social_service.delivery.http.user.response.UnfollowUserResponse;
import com.twohands.social_service.delivery.http.user.response.ViewSocialProfileResponse;
import com.twohands.social_service.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/social/users")
public class UserController {

    private final FollowUserUseCase followUserUseCase;
    private final UnfollowUserUseCase unfollowUserUseCase;
    private final ViewSocialProfileUseCase viewSocialProfileUseCase;

    public UserController(
            FollowUserUseCase followUserUseCase,
            UnfollowUserUseCase unfollowUserUseCase,
            ViewSocialProfileUseCase viewSocialProfileUseCase
    ) {
        this.followUserUseCase = followUserUseCase;
        this.unfollowUserUseCase = unfollowUserUseCase;
        this.viewSocialProfileUseCase = viewSocialProfileUseCase;
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<ApiResponse<ViewSocialProfileResponse>> viewSocialProfile(
            @PathVariable UUID userId,
            Authentication authentication
    ) {
        UUID viewerId = resolveUserId(authentication);
        ViewSocialProfileResult result = viewSocialProfileUseCase.execute(
                new ViewSocialProfileCommand(viewerId, userId)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewSocialProfileUseCase.successMessage(),
                toProfileResponse(result)
        ));
    }

    @PostMapping("/{userId}/follow")
    public ResponseEntity<ApiResponse<FollowUserResponse>> followUser(
            @PathVariable UUID userId,
            Authentication authentication
    ) {
        UUID followerId = resolveUserId(authentication);
        FollowUserResult result = followUserUseCase.execute(new FollowUserCommand(followerId, userId));
        FollowUserResponse response = toResponse(result);

        HttpStatus httpStatus = result.newlyCreated() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(httpStatus).body(ApiResponse.success(
                httpStatus.value(),
                followUserUseCase.successMessage(),
                response
        ));
    }

    @DeleteMapping("/{userId}/follow")
    public ResponseEntity<ApiResponse<UnfollowUserResponse>> unfollowUser(
            @PathVariable UUID userId,
            Authentication authentication
    ) {
        UUID followerId = resolveUserId(authentication);
        UnfollowUserResult result = unfollowUserUseCase.execute(new UnfollowUserCommand(followerId, userId));

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                unfollowUserUseCase.successMessage(),
                toUnfollowResponse(result)
        ));
    }

    private UUID resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            return null;
        }
        return principal.userId();
    }

    private FollowUserResponse toResponse(FollowUserResult result) {
        return new FollowUserResponse(
                result.followeeId().toString(),
                result.status().name(),
                result.createdAt()
        );
    }

    private UnfollowUserResponse toUnfollowResponse(UnfollowUserResult result) {
        return new UnfollowUserResponse(
                result.followeeId().toString(),
                result.wasFollowing()
        );
    }

    private ViewSocialProfileResponse toProfileResponse(ViewSocialProfileResult result) {
        return new ViewSocialProfileResponse(
                result.userId(),
                result.displayName(),
                result.avatarUrl(),
                result.isPrivate(),
                result.followerCount(),
                result.followingCount(),
                result.followStatus(),
                result.canViewFullProfile()
        );
    }
}
