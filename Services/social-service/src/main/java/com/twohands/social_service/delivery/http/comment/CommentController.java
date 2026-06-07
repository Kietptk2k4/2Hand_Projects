package com.twohands.social_service.delivery.http.comment;

import com.twohands.social_service.application.comment.deleteowncomment.DeleteOwnCommentCommand;
import com.twohands.social_service.application.comment.deleteowncomment.DeleteOwnCommentResult;
import com.twohands.social_service.application.comment.deleteowncomment.DeleteOwnCommentUseCase;
import com.twohands.social_service.application.comment.likecomment.LikeCommentCommand;
import com.twohands.social_service.application.comment.likecomment.LikeCommentResult;
import com.twohands.social_service.application.comment.likecomment.LikeCommentUseCase;
import com.twohands.social_service.application.comment.replycomment.ReplyCommentCommand;
import com.twohands.social_service.application.comment.replycomment.ReplyCommentResult;
import com.twohands.social_service.application.comment.replycomment.ReplyCommentUseCase;
import com.twohands.social_service.common.dto.ApiResponse;
import com.twohands.social_service.delivery.http.comment.request.ReplyCommentRequest;
import com.twohands.social_service.delivery.http.comment.response.DeleteOwnCommentResponse;
import com.twohands.social_service.delivery.http.comment.response.LikeCommentResponse;
import com.twohands.social_service.delivery.http.comment.response.ReplyCommentResponse;
import com.twohands.social_service.domain.comment.CommentMediaItem;
import com.twohands.social_service.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/social/comments")
public class CommentController {

    private final ReplyCommentUseCase replyCommentUseCase;
    private final DeleteOwnCommentUseCase deleteOwnCommentUseCase;
    private final LikeCommentUseCase likeCommentUseCase;

    public CommentController(
            ReplyCommentUseCase replyCommentUseCase,
            DeleteOwnCommentUseCase deleteOwnCommentUseCase,
            LikeCommentUseCase likeCommentUseCase
    ) {
        this.replyCommentUseCase = replyCommentUseCase;
        this.deleteOwnCommentUseCase = deleteOwnCommentUseCase;
        this.likeCommentUseCase = likeCommentUseCase;
    }

    @PostMapping("/{commentId}/replies")
    public ResponseEntity<ApiResponse<ReplyCommentResponse>> replyComment(
            @PathVariable String commentId,
            @RequestBody @Valid ReplyCommentRequest request,
            Authentication authentication
    ) {
        UUID authorId = resolveUserId(authentication);
        ReplyCommentCommand command = toCommand(request, authorId, commentId);
        ReplyCommentResult result = replyCommentUseCase.execute(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        replyCommentUseCase.successMessage(),
                        toResponse(result)
                ));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<DeleteOwnCommentResponse>> deleteOwnComment(
            @PathVariable String commentId,
            Authentication authentication
    ) {
        AuthenticatedUser actor = resolveActor(authentication);
        DeleteOwnCommentCommand command = new DeleteOwnCommentCommand(
                actor != null ? actor.userId() : null,
                actor != null ? actor.roles() : List.of(),
                commentId
        );
        DeleteOwnCommentResult result = deleteOwnCommentUseCase.execute(command);

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                deleteOwnCommentUseCase.successMessage(),
                toDeleteResponse(result)
        ));
    }

    @PostMapping("/{commentId}/like")
    public ResponseEntity<ApiResponse<LikeCommentResponse>> likeComment(
            @PathVariable String commentId,
            Authentication authentication
    ) {
        UUID userId = resolveUserId(authentication);
        LikeCommentResult result = likeCommentUseCase.execute(new LikeCommentCommand(userId, commentId));

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                likeCommentUseCase.successMessage(result.liked()),
                toLikeResponse(result)
        ));
    }

    private AuthenticatedUser resolveActor(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            return null;
        }
        return principal;
    }

    private UUID resolveUserId(Authentication authentication) {
        AuthenticatedUser actor = resolveActor(authentication);
        return actor != null ? actor.userId() : null;
    }

    private ReplyCommentCommand toCommand(ReplyCommentRequest request, UUID authorId, String parentCommentId) {
        List<CommentMediaItem> media = request.media() != null
                ? request.media().stream()
                        .map(m -> new CommentMediaItem(m.url(), m.type()))
                        .toList()
                : List.of();
        return new ReplyCommentCommand(authorId, parentCommentId, request.contentText(), media);
    }

    private ReplyCommentResponse toResponse(ReplyCommentResult result) {
        List<ReplyCommentResponse.MediaItemResponse> media = result.media().stream()
                .map(m -> new ReplyCommentResponse.MediaItemResponse(m.url(), m.type()))
                .toList();
        ReplyCommentResponse.AuthorResponse author = new ReplyCommentResponse.AuthorResponse(
                result.author().userId(),
                result.author().displayName(),
                result.author().avatarUrl()
        );
        return new ReplyCommentResponse(
                result.commentId(),
                result.postId(),
                result.parentCommentId(),
                result.authorId(),
                author,
                result.contentText(),
                media,
                result.status(),
                result.createdAt(),
                result.updatedAt()
        );
    }

    private DeleteOwnCommentResponse toDeleteResponse(DeleteOwnCommentResult result) {
        return new DeleteOwnCommentResponse(
                result.commentId(),
                result.postId(),
                result.status(),
                result.deletedAt(),
                result.updatedAt()
        );
    }

    private LikeCommentResponse toLikeResponse(LikeCommentResult result) {
        return new LikeCommentResponse(
                result.commentId(),
                result.liked(),
                result.likeCount()
        );
    }
}
