package com.twohands.social_service.delivery.http.admin;

import com.twohands.social_service.application.admin.viewcommentdetailformoderation.ViewCommentDetailForModerationCommand;
import com.twohands.social_service.application.admin.viewcommentdetailformoderation.ViewCommentDetailForModerationResult;
import com.twohands.social_service.application.admin.viewcommentdetailformoderation.ViewCommentDetailForModerationUseCase;
import com.twohands.social_service.application.admin.viewcommentlistformoderation.ViewCommentListForModerationCommand;
import com.twohands.social_service.application.admin.viewcommentlistformoderation.ViewCommentListForModerationResult;
import com.twohands.social_service.application.admin.viewcommentlistformoderation.ViewCommentListForModerationUseCase;
import com.twohands.social_service.application.admin.viewpostdetailformoderation.ViewPostDetailForModerationCommand;
import com.twohands.social_service.application.admin.viewpostdetailformoderation.ViewPostDetailForModerationResult;
import com.twohands.social_service.application.admin.viewpostdetailformoderation.ViewPostDetailForModerationUseCase;
import com.twohands.social_service.application.admin.viewpostlistformoderation.ViewPostListForModerationCommand;
import com.twohands.social_service.application.admin.viewpostlistformoderation.ViewPostListForModerationResult;
import com.twohands.social_service.application.admin.viewpostlistformoderation.ViewPostListForModerationUseCase;
import com.twohands.social_service.common.dto.ApiResponse;
import com.twohands.social_service.delivery.http.admin.response.ViewCommentDetailForModerationResponse;
import com.twohands.social_service.delivery.http.admin.response.ViewCommentListForModerationResponse;
import com.twohands.social_service.delivery.http.admin.response.ViewPostDetailForModerationResponse;
import com.twohands.social_service.delivery.http.admin.response.ViewPostListForModerationResponse;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import com.twohands.social_service.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/social/admin")
public class SocialAdminModerationController {

    private final ViewPostListForModerationUseCase viewPostListForModerationUseCase;
    private final ViewPostDetailForModerationUseCase viewPostDetailForModerationUseCase;
    private final ViewCommentListForModerationUseCase viewCommentListForModerationUseCase;
    private final ViewCommentDetailForModerationUseCase viewCommentDetailForModerationUseCase;

    public SocialAdminModerationController(
            ViewPostListForModerationUseCase viewPostListForModerationUseCase,
            ViewPostDetailForModerationUseCase viewPostDetailForModerationUseCase,
            ViewCommentListForModerationUseCase viewCommentListForModerationUseCase,
            ViewCommentDetailForModerationUseCase viewCommentDetailForModerationUseCase
    ) {
        this.viewPostListForModerationUseCase = viewPostListForModerationUseCase;
        this.viewPostDetailForModerationUseCase = viewPostDetailForModerationUseCase;
        this.viewCommentListForModerationUseCase = viewCommentListForModerationUseCase;
        this.viewCommentDetailForModerationUseCase = viewCommentDetailForModerationUseCase;
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

    @GetMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<ViewPostDetailForModerationResponse>> viewPostDetail(
            @PathVariable String postId,
            Authentication authentication
    ) {
        ViewPostDetailForModerationResult result = viewPostDetailForModerationUseCase.execute(
                new ViewPostDetailForModerationCommand(resolveActor(authentication), postId)
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        viewPostDetailForModerationUseCase.successMessage(),
                        toPostDetailResponse(result)
                ));
    }

    @GetMapping("/comments")
    public ResponseEntity<ApiResponse<ViewCommentListForModerationResponse>> viewCommentList(
            @RequestParam(required = false) String status,
            @RequestParam(name = "moderation_status", required = false) String moderationStatus,
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
                        moderationStatus,
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

    @GetMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<ViewCommentDetailForModerationResponse>> viewCommentDetail(
            @PathVariable String commentId,
            Authentication authentication
    ) {
        ViewCommentDetailForModerationResult result = viewCommentDetailForModerationUseCase.execute(
                new ViewCommentDetailForModerationCommand(resolveActor(authentication), commentId)
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        viewCommentDetailForModerationUseCase.successMessage(),
                        toCommentDetailResponse(result)
                ));
    }

    private ViewPostListForModerationResponse toPostResponse(ViewPostListForModerationResult result) {
        return new ViewPostListForModerationResponse(
                result.items().stream()
                        .map(item -> new ViewPostListForModerationResponse.ItemData(
                                item.id(),
                                item.authorId(),
                                item.authorDisplayName(),
                                item.authorAvatarUrl(),
                                item.captionPreview(),
                                item.thumbnailUrl(),
                                item.mediaCount(),
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

    private ViewPostDetailForModerationResponse toPostDetailResponse(ViewPostDetailForModerationResult result) {
        return new ViewPostDetailForModerationResponse(
                result.id(),
                new ViewPostDetailForModerationResponse.AuthorData(
                        result.author().userId(),
                        result.author().displayName(),
                        result.author().avatarUrl()
                ),
                result.caption(),
                result.media().stream()
                        .map(item -> new ViewPostDetailForModerationResponse.MediaData(
                                item.url(),
                                item.type(),
                                item.width(),
                                item.height()
                        ))
                        .toList(),
                result.thumbnailUrl(),
                result.mediaCount(),
                result.status(),
                result.moderationStatus(),
                result.moderationReason(),
                result.lastModerationLogId(),
                result.visibility(),
                result.likeCount(),
                result.replyCount(),
                result.hashtags(),
                result.allowComments(),
                result.createdAt(),
                result.updatedAt()
        );
    }

    private ViewCommentListForModerationResponse toCommentResponse(ViewCommentListForModerationResult result) {
        return new ViewCommentListForModerationResponse(
                result.items().stream()
                        .map(item -> new ViewCommentListForModerationResponse.ItemData(
                                item.id(),
                                item.postId(),
                                item.authorId(),
                                item.authorDisplayName(),
                                item.authorAvatarUrl(),
                                item.parentCommentId(),
                                item.contentPreview(),
                                item.status(),
                                item.moderationStatus(),
                                item.mediaCount(),
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

    private ViewCommentDetailForModerationResponse toCommentDetailResponse(ViewCommentDetailForModerationResult result) {
        ViewCommentDetailForModerationResult.ParentCommentSummary parent = result.parentComment();
        ViewCommentDetailForModerationResult.PostContextSummary post = result.post();

        return new ViewCommentDetailForModerationResponse(
                result.id(),
                result.postId(),
                new ViewCommentDetailForModerationResponse.AuthorData(
                        result.author().userId(),
                        result.author().displayName(),
                        result.author().avatarUrl()
                ),
                result.parentCommentId(),
                parent == null ? null : new ViewCommentDetailForModerationResponse.ParentCommentData(
                        parent.id(),
                        parent.contentPreview()
                ),
                result.contentText(),
                result.media().stream()
                        .map(item -> new ViewCommentDetailForModerationResponse.MediaData(item.url(), item.type()))
                        .toList(),
                result.mediaCount(),
                result.status(),
                result.moderationStatus(),
                result.moderationReason(),
                result.lastModerationLogId(),
                result.likeCount(),
                post == null ? null : new ViewCommentDetailForModerationResponse.PostContextData(
                        post.id(),
                        post.captionPreview(),
                        post.thumbnailUrl(),
                        post.moderationStatus()
                ),
                result.createdAt(),
                result.updatedAt()
        );
    }

    private AuthenticatedUser resolveActor(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.defaultMessage());
        }
        return principal;
    }
}
