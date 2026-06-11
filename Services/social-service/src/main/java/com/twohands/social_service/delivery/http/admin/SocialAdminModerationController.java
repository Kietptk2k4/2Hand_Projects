package com.twohands.social_service.delivery.http.admin;

import com.twohands.social_service.application.admin.viewcommentlistformoderation.ViewCommentListForModerationCommand;
import com.twohands.social_service.application.admin.viewcommentlistformoderation.ViewCommentListForModerationResult;
import com.twohands.social_service.application.admin.viewcommentlistformoderation.ViewCommentListForModerationUseCase;
import com.twohands.social_service.application.admin.viewpostlistformoderation.ViewPostListForModerationCommand;
import com.twohands.social_service.application.admin.viewpostlistformoderation.ViewPostListForModerationResult;
import com.twohands.social_service.application.admin.viewpostlistformoderation.ViewPostListForModerationUseCase;
import com.twohands.social_service.common.dto.ApiResponse;
import com.twohands.social_service.delivery.http.admin.response.ViewCommentListForModerationResponse;
import com.twohands.social_service.delivery.http.admin.response.ViewPostListForModerationResponse;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import com.twohands.social_service.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/social/admin")
public class SocialAdminModerationController {

    private final ViewPostListForModerationUseCase viewPostListForModerationUseCase;
    private final ViewCommentListForModerationUseCase viewCommentListForModerationUseCase;

    public SocialAdminModerationController(
            ViewPostListForModerationUseCase viewPostListForModerationUseCase,
            ViewCommentListForModerationUseCase viewCommentListForModerationUseCase
    ) {
        this.viewPostListForModerationUseCase = viewPostListForModerationUseCase;
        this.viewCommentListForModerationUseCase = viewCommentListForModerationUseCase;
    }

    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<ViewPostListForModerationResponse>> viewPostList(
            @RequestParam(required = false) String status,
            @RequestParam(name = "moderation_status", required = false) String moderationStatus,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        ViewPostListForModerationResult result = viewPostListForModerationUseCase.execute(
                new ViewPostListForModerationCommand(
                        resolveActor(authentication),
                        status,
                        moderationStatus,
                        q,
                        sort,
                        page,
                        size
                )
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        viewPostListForModerationUseCase.successMessage(),
                        toPostResponse(result)
                ));
    }

    @GetMapping("/comments")
    public ResponseEntity<ApiResponse<ViewCommentListForModerationResponse>> viewCommentList(
            @RequestParam(required = false) String status,
            @RequestParam(name = "post_id", required = false) String postId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        ViewCommentListForModerationResult result = viewCommentListForModerationUseCase.execute(
                new ViewCommentListForModerationCommand(
                        resolveActor(authentication),
                        status,
                        postId,
                        q,
                        sort,
                        page,
                        size
                )
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        viewCommentListForModerationUseCase.successMessage(),
                        toCommentResponse(result)
                ));
    }

    private ViewPostListForModerationResponse toPostResponse(ViewPostListForModerationResult result) {
        return new ViewPostListForModerationResponse(
                result.items().stream()
                        .map(item -> new ViewPostListForModerationResponse.ItemData(
                                item.id(),
                                item.authorId(),
                                item.captionPreview(),
                                item.status(),
                                item.moderationStatus(),
                                item.likeCount(),
                                item.createdAt(),
                                item.updatedAt()
                        ))
                        .toList(),
                new ViewPostListForModerationResponse.PaginationData(
                        result.pagination().page(),
                        result.pagination().size(),
                        result.pagination().totalItems(),
                        result.pagination().totalPages(),
                        result.pagination().hasNext()
                )
        );
    }

    private ViewCommentListForModerationResponse toCommentResponse(ViewCommentListForModerationResult result) {
        return new ViewCommentListForModerationResponse(
                result.items().stream()
                        .map(item -> new ViewCommentListForModerationResponse.ItemData(
                                item.id(),
                                item.postId(),
                                item.authorId(),
                                item.contentPreview(),
                                item.status(),
                                item.likeCount(),
                                item.createdAt(),
                                item.updatedAt()
                        ))
                        .toList(),
                new ViewCommentListForModerationResponse.PaginationData(
                        result.pagination().page(),
                        result.pagination().size(),
                        result.pagination().totalItems(),
                        result.pagination().totalPages(),
                        result.pagination().hasNext()
                )
        );
    }

    private AuthenticatedUser resolveActor(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.defaultMessage());
        }
        return principal;
    }
}
