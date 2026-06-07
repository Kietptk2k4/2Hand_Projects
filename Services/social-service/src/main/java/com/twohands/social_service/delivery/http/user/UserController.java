package com.twohands.social_service.delivery.http.user;

import com.twohands.social_service.application.user.followuser.FollowUserCommand;
import com.twohands.social_service.application.user.followuser.FollowUserResult;
import com.twohands.social_service.application.user.followuser.FollowUserUseCase;
import com.twohands.social_service.application.user.unfollowuser.UnfollowUserCommand;
import com.twohands.social_service.application.user.unfollowuser.UnfollowUserResult;
import com.twohands.social_service.application.user.unfollowuser.UnfollowUserUseCase;
import com.twohands.social_service.application.user.viewfollowersfollowinglist.ViewFollowersFollowingListCommand;
import com.twohands.social_service.application.user.viewfollowersfollowinglist.ViewFollowersFollowingListResult;
import com.twohands.social_service.application.user.viewfollowersfollowinglist.ViewFollowersFollowingListUseCase;
import com.twohands.social_service.application.user.viewsuggestedusers.ViewSuggestedUsersCommand;
import com.twohands.social_service.application.user.viewsuggestedusers.ViewSuggestedUsersResult;
import com.twohands.social_service.application.user.viewsuggestedusers.ViewSuggestedUsersUseCase;
import com.twohands.social_service.application.user.viewsocialprofile.ViewSocialProfileCommand;
import com.twohands.social_service.application.user.viewsocialprofile.ViewSocialProfileResult;
import com.twohands.social_service.application.user.viewsocialprofile.ViewSocialProfileUseCase;
import com.twohands.social_service.application.user.viewuserposts.ViewUserPostsResult;
import com.twohands.social_service.application.user.viewuserposts.ViewUserPostsUseCase;
import com.twohands.social_service.common.dto.ApiResponse;
import com.twohands.social_service.delivery.http.user.response.FollowUserResponse;
import com.twohands.social_service.delivery.http.user.response.UnfollowUserResponse;
import com.twohands.social_service.delivery.http.user.response.ViewFollowersFollowingListResponse;
import com.twohands.social_service.delivery.http.user.mapper.ViewUserPostsHttpMapper;
import com.twohands.social_service.delivery.http.user.response.ViewSocialProfileResponse;
import com.twohands.social_service.delivery.http.user.response.ViewSuggestedUsersResponse;
import com.twohands.social_service.delivery.http.user.response.ViewUserPostsResponse;
import com.twohands.social_service.domain.follow.RelationListType;
import com.twohands.social_service.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/social/users")
public class UserController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final String DEFAULT_STATUS_FILTER = "published";

    private final FollowUserUseCase followUserUseCase;
    private final UnfollowUserUseCase unfollowUserUseCase;
    private final ViewSocialProfileUseCase viewSocialProfileUseCase;
    private final ViewUserPostsUseCase viewUserPostsUseCase;
    private final ViewUserPostsHttpMapper viewUserPostsHttpMapper;
    private final ViewFollowersFollowingListUseCase viewFollowersFollowingListUseCase;
    private final ViewSuggestedUsersUseCase viewSuggestedUsersUseCase;

    public UserController(
            FollowUserUseCase followUserUseCase,
            UnfollowUserUseCase unfollowUserUseCase,
            ViewSocialProfileUseCase viewSocialProfileUseCase,
            ViewUserPostsUseCase viewUserPostsUseCase,
            ViewUserPostsHttpMapper viewUserPostsHttpMapper,
            ViewFollowersFollowingListUseCase viewFollowersFollowingListUseCase,
            ViewSuggestedUsersUseCase viewSuggestedUsersUseCase
    ) {
        this.followUserUseCase = followUserUseCase;
        this.unfollowUserUseCase = unfollowUserUseCase;
        this.viewSocialProfileUseCase = viewSocialProfileUseCase;
        this.viewUserPostsUseCase = viewUserPostsUseCase;
        this.viewUserPostsHttpMapper = viewUserPostsHttpMapper;
        this.viewFollowersFollowingListUseCase = viewFollowersFollowingListUseCase;
        this.viewSuggestedUsersUseCase = viewSuggestedUsersUseCase;
    }

    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<ViewSuggestedUsersResponse>> viewSuggestedUsers(
            @RequestParam(name = "page", defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(name = "size", required = false) Integer size,
            @RequestParam(name = "limit", required = false) Integer limit,
            Authentication authentication
    ) {
        UUID viewerId = resolveUserId(authentication);
        int resolvedSize = resolveSuggestionSize(size, limit);
        ViewSuggestedUsersResult result = viewSuggestedUsersUseCase.execute(
                new ViewSuggestedUsersCommand(viewerId, page, resolvedSize)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewSuggestedUsersUseCase.successMessage(),
                toSuggestedUsersResponse(result)
        ));
    }

    @GetMapping("/{userId}/relations")
    public ResponseEntity<ApiResponse<ViewFollowersFollowingListResponse>> viewFollowersFollowingList(
            @PathVariable UUID userId,
            @RequestParam String type,
            @RequestParam(name = "page", defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(name = "size", defaultValue = "" + DEFAULT_SIZE) int size,
            Authentication authentication
    ) {
        UUID viewerId = resolveUserId(authentication);
        RelationListType relationType = RelationListType.fromQuery(type);
        ViewFollowersFollowingListResult result = viewFollowersFollowingListUseCase.execute(
                new ViewFollowersFollowingListCommand(viewerId, userId, relationType, page, size)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewFollowersFollowingListUseCase.successMessage(),
                toRelationsResponse(result)
        ));
    }

    @GetMapping("/{userId}/posts")
    public ResponseEntity<ApiResponse<ViewUserPostsResponse>> viewUserPosts(
            @PathVariable UUID userId,
            @RequestParam(name = "page", defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(name = "size", defaultValue = "" + DEFAULT_SIZE) int size,
            @RequestParam(name = "status_filter", defaultValue = DEFAULT_STATUS_FILTER) String statusFilter,
            Authentication authentication
    ) {
        UUID viewerId = resolveUserId(authentication);
        ViewUserPostsResult result = viewUserPostsUseCase.execute(viewerId, userId, page, size, statusFilter);

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewUserPostsUseCase.successMessage(),
                viewUserPostsHttpMapper.toResponse(result)
        ));
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

    private int resolveSuggestionSize(Integer size, Integer limit) {
        if (size != null) {
            return size;
        }
        if (limit != null) {
            return limit;
        }
        return DEFAULT_SIZE;
    }

    private ViewSuggestedUsersResponse toSuggestedUsersResponse(ViewSuggestedUsersResult result) {
        List<ViewSuggestedUsersResponse.SuggestedUserItemResponse> items = result.users().items().stream()
                .map(item -> new ViewSuggestedUsersResponse.SuggestedUserItemResponse(
                        item.userId(),
                        item.displayName(),
                        item.avatarUrl(),
                        item.followStatus(),
                        item.mutualFollowCount()
                ))
                .toList();
        return new ViewSuggestedUsersResponse(
                items,
                new ViewSuggestedUsersResponse.PageMetaResponse(
                        result.users().page(),
                        result.users().size(),
                        result.users().totalElements(),
                        result.users().totalPages(),
                        result.users().hasNext()
                )
        );
    }

    private ViewFollowersFollowingListResponse toRelationsResponse(ViewFollowersFollowingListResult result) {
        List<ViewFollowersFollowingListResponse.RelationUserItemResponse> items = result.users().items().stream()
                .map(item -> new ViewFollowersFollowingListResponse.RelationUserItemResponse(
                        item.userId(),
                        item.displayName(),
                        item.avatarUrl(),
                        item.followedAt() != null ? DateTimeFormatter.ISO_INSTANT.format(item.followedAt()) : null
                ))
                .toList();
        return new ViewFollowersFollowingListResponse(
                result.targetUserId(),
                result.type().queryValue(),
                items,
                new ViewFollowersFollowingListResponse.PageMetaResponse(
                        result.users().page(),
                        result.users().size(),
                        result.users().totalElements(),
                        result.users().totalPages(),
                        result.users().hasNext()
                )
        );
    }
}
