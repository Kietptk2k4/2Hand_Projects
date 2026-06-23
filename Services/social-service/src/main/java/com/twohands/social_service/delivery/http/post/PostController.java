package com.twohands.social_service.delivery.http.post;

import com.twohands.social_service.application.comment.commentpost.CommentPostCommand;
import com.twohands.social_service.application.comment.commentpost.CommentPostResult;
import com.twohands.social_service.application.comment.commentpost.CommentPostUseCase;
import com.twohands.social_service.application.comment.listpostcomments.ListPostCommentsResult;
import com.twohands.social_service.application.comment.listpostcomments.ListPostCommentsUseCase;
import com.twohands.social_service.application.post.createpost.CreatePostCommand;
import com.twohands.social_service.application.post.createpost.CreatePostResult;
import com.twohands.social_service.application.post.createpost.CreatePostUseCase;
import com.twohands.social_service.application.post.deletepost.DeletePostCommand;
import com.twohands.social_service.application.post.deletepost.DeletePostResult;
import com.twohands.social_service.application.post.deletepost.DeletePostUseCase;
import com.twohands.social_service.application.post.editpost.EditPostCommand;
import com.twohands.social_service.application.post.editpost.EditPostResult;
import com.twohands.social_service.application.post.editpost.EditPostUseCase;
import com.twohands.social_service.application.post.likeunlikepost.LikeUnlikePostCommand;
import com.twohands.social_service.application.post.likeunlikepost.LikeUnlikePostResult;
import com.twohands.social_service.application.post.likeunlikepost.LikeUnlikePostUseCase;
import com.twohands.social_service.application.post.saveunsavepost.SaveUnsavePostCommand;
import com.twohands.social_service.application.post.saveunsavepost.SaveUnsavePostResult;
import com.twohands.social_service.application.post.saveunsavepost.SaveUnsavePostUseCase;
import com.twohands.social_service.application.post.uploadpostmedia.UploadPostMediaCommand;
import com.twohands.social_service.application.post.uploadpostmedia.UploadPostMediaResult;
import com.twohands.social_service.application.post.uploadpostmedia.UploadPostMediaUseCase;
import com.twohands.social_service.application.post.viewpostdetail.ViewPostDetailResult;
import com.twohands.social_service.application.post.viewpostdetail.ViewPostDetailUseCase;
import com.twohands.social_service.application.post.viewpostlikers.ViewPostLikersUseCase;
import com.twohands.social_service.application.post.viewsavedposts.ViewSavedPostsResult;
import com.twohands.social_service.application.post.viewsavedposts.ViewSavedPostsUseCase;
import com.twohands.social_service.application.reaction.common.ViewLikeUsersResult;
import com.twohands.social_service.common.dto.ApiResponse;
import com.twohands.social_service.domain.comment.CommentMediaItem;
import com.twohands.social_service.delivery.http.comment.mapper.ListPostCommentsHttpMapper;
import com.twohands.social_service.delivery.http.comment.request.CommentPostRequest;
import com.twohands.social_service.delivery.http.comment.response.CommentPostResponse;
import com.twohands.social_service.delivery.http.comment.response.ListPostCommentsResponse;
import com.twohands.social_service.delivery.http.post.request.CreatePostRequest;
import com.twohands.social_service.delivery.http.post.request.EditPostRequest;
import com.twohands.social_service.delivery.http.post.request.UploadPostMediaRequest;
import com.twohands.social_service.delivery.http.post.response.CreatePostResponse;
import com.twohands.social_service.delivery.http.post.response.DeletePostResponse;
import com.twohands.social_service.delivery.http.post.response.EditPostResponse;
import com.twohands.social_service.delivery.http.post.response.LikeUnlikePostResponse;
import com.twohands.social_service.delivery.http.post.mapper.ViewPostDetailHttpMapper;
import com.twohands.social_service.delivery.http.post.mapper.ViewSavedPostsHttpMapper;
import com.twohands.social_service.delivery.http.post.response.SaveUnsavePostResponse;
import com.twohands.social_service.delivery.http.post.response.UploadPostMediaResponse;
import com.twohands.social_service.delivery.http.post.response.ViewPostDetailResponse;
import com.twohands.social_service.delivery.http.post.response.ViewSavedPostsResponse;
import com.twohands.social_service.delivery.http.reaction.mapper.ViewLikeUsersHttpMapper;
import com.twohands.social_service.delivery.http.reaction.response.ViewLikeUsersResponse;
import com.twohands.social_service.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/social/posts")
public class PostController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final String DEFAULT_SORT = "created_at_asc";

    private final CreatePostUseCase createPostUseCase;
    private final EditPostUseCase editPostUseCase;
    private final DeletePostUseCase deletePostUseCase;
    private final LikeUnlikePostUseCase likeUnlikePostUseCase;
    private final SaveUnsavePostUseCase saveUnsavePostUseCase;
    private final CommentPostUseCase commentPostUseCase;
    private final ListPostCommentsUseCase listPostCommentsUseCase;
    private final ListPostCommentsHttpMapper listPostCommentsHttpMapper;
    private final ViewSavedPostsUseCase viewSavedPostsUseCase;
    private final ViewSavedPostsHttpMapper viewSavedPostsHttpMapper;
    private final ViewPostDetailUseCase viewPostDetailUseCase;
    private final ViewPostDetailHttpMapper viewPostDetailHttpMapper;
    private final ViewPostLikersUseCase viewPostLikersUseCase;
    private final ViewLikeUsersHttpMapper viewLikeUsersHttpMapper;
    private final UploadPostMediaUseCase uploadPostMediaUseCase;

    public PostController(
            CreatePostUseCase createPostUseCase,
            EditPostUseCase editPostUseCase,
            DeletePostUseCase deletePostUseCase,
            LikeUnlikePostUseCase likeUnlikePostUseCase,
            SaveUnsavePostUseCase saveUnsavePostUseCase,
            CommentPostUseCase commentPostUseCase,
            ListPostCommentsUseCase listPostCommentsUseCase,
            ListPostCommentsHttpMapper listPostCommentsHttpMapper,
            ViewSavedPostsUseCase viewSavedPostsUseCase,
            ViewSavedPostsHttpMapper viewSavedPostsHttpMapper,
            ViewPostDetailUseCase viewPostDetailUseCase,
            ViewPostDetailHttpMapper viewPostDetailHttpMapper,
            ViewPostLikersUseCase viewPostLikersUseCase,
            ViewLikeUsersHttpMapper viewLikeUsersHttpMapper,
            UploadPostMediaUseCase uploadPostMediaUseCase
    ) {
        this.createPostUseCase = createPostUseCase;
        this.editPostUseCase = editPostUseCase;
        this.deletePostUseCase = deletePostUseCase;
        this.likeUnlikePostUseCase = likeUnlikePostUseCase;
        this.saveUnsavePostUseCase = saveUnsavePostUseCase;
        this.commentPostUseCase = commentPostUseCase;
        this.listPostCommentsUseCase = listPostCommentsUseCase;
        this.listPostCommentsHttpMapper = listPostCommentsHttpMapper;
        this.viewSavedPostsUseCase = viewSavedPostsUseCase;
        this.viewSavedPostsHttpMapper = viewSavedPostsHttpMapper;
        this.viewPostDetailUseCase = viewPostDetailUseCase;
        this.viewPostDetailHttpMapper = viewPostDetailHttpMapper;
        this.viewPostLikersUseCase = viewPostLikersUseCase;
        this.viewLikeUsersHttpMapper = viewLikeUsersHttpMapper;
        this.uploadPostMediaUseCase = uploadPostMediaUseCase;
    }

    @GetMapping("/saved")
    public ResponseEntity<ApiResponse<ViewSavedPostsResponse>> viewSavedPosts(
            @RequestParam(name = "page", defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(name = "size", defaultValue = "" + DEFAULT_SIZE) int size,
            Authentication authentication
    ) {
        UUID userId = resolveUserId(authentication);
        ViewSavedPostsResult result = viewSavedPostsUseCase.execute(userId, page, size);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        viewSavedPostsUseCase.successMessage(),
                        viewSavedPostsHttpMapper.toResponse(result)
                ));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<ViewPostDetailResponse>> viewPostDetail(
            @PathVariable String postId,
            Authentication authentication
    ) {
        UUID viewerId = resolveUserId(authentication);
        ViewPostDetailResult result = viewPostDetailUseCase.execute(viewerId, postId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        viewPostDetailUseCase.successMessage(),
                        viewPostDetailHttpMapper.toResponse(result)
                ));
    }

    @PostMapping("/media/upload-url")
    public ResponseEntity<ApiResponse<UploadPostMediaResponse>> createPostMediaUploadUrl(
            @RequestBody @Valid UploadPostMediaRequest request,
            Authentication authentication
    ) {
        UUID userId = resolveUserId(authentication);
        UploadPostMediaResult result = uploadPostMediaUseCase.execute(
                new UploadPostMediaCommand(
                        userId,
                        request.contentType(),
                        request.fileSizeBytes(),
                        request.mediaKind(),
                        request.clientUploadOrigin()
                )
        );

        UploadPostMediaResponse response = new UploadPostMediaResponse(
                result.uploadUrl(),
                result.objectKey(),
                result.mediaUrl(),
                result.mediaKind(),
                result.expiresAt().toString(),
                result.maxFileSizeBytes(),
                result.allowedContentTypes()
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                uploadPostMediaUseCase.successMessage(),
                response
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreatePostResponse>> createPost(
            @RequestBody @Valid CreatePostRequest request,
            Authentication authentication
    ) {
        UUID authorId = resolveUserId(authentication);
        CreatePostCommand command = toCommand(request, authorId);
        CreatePostResult result = createPostUseCase.execute(command);
        CreatePostResponse response = toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        createPostUseCase.successMessage(),
                        response
                ));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<EditPostResponse>> editPost(
            @PathVariable String postId,
            @RequestBody @Valid EditPostRequest request,
            Authentication authentication
    ) {
        UUID editorId = resolveUserId(authentication);
        EditPostCommand command = toEditCommand(request, editorId, postId);
        EditPostResult result = editPostUseCase.execute(command);
        EditPostResponse response = toEditResponse(result);

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                editPostUseCase.successMessage(),
                response
        ));
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<ListPostCommentsResponse>> listPostComments(
            @PathVariable String postId,
            @RequestParam(name = "page", defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(name = "size", defaultValue = "" + DEFAULT_SIZE) int size,
            @RequestParam(name = "parent_comment_id", required = false) String parentCommentId,
            @RequestParam(name = "sort", defaultValue = DEFAULT_SORT) String sort,
            Authentication authentication
    ) {
        UUID viewerId = resolveUserId(authentication);
        ListPostCommentsResult result = listPostCommentsUseCase.execute(
                viewerId,
                postId,
                page,
                size,
                parentCommentId,
                sort
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                listPostCommentsUseCase.successMessage(),
                listPostCommentsHttpMapper.toResponse(result)
        ));
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentPostResponse>> commentPost(
            @PathVariable String postId,
            @RequestBody @Valid CommentPostRequest request,
            Authentication authentication
    ) {
        UUID authorId = resolveUserId(authentication);
        CommentPostCommand command = toCommentPostCommand(request, authorId, postId);
        CommentPostResult result = commentPostUseCase.execute(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        commentPostUseCase.successMessage(),
                        toCommentPostResponse(result)
                ));
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<LikeUnlikePostResponse>> likeUnlikePost(
            @PathVariable String postId,
            Authentication authentication
    ) {
        UUID userId = resolveUserId(authentication);
        LikeUnlikePostResult result = likeUnlikePostUseCase.execute(new LikeUnlikePostCommand(userId, postId));

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                likeUnlikePostUseCase.successMessage(result.liked()),
                toLikeUnlikeResponse(result)
        ));
    }

    @GetMapping("/{postId}/likes")
    public ResponseEntity<ApiResponse<ViewLikeUsersResponse>> viewPostLikers(
            @PathVariable String postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        UUID viewerId = resolveUserId(authentication);
        ViewLikeUsersResult result = viewPostLikersUseCase.execute(viewerId, postId, page, size);

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewPostLikersUseCase.successMessage(),
                viewLikeUsersHttpMapper.toResponse(result)
        ));
    }

    @PostMapping("/{postId}/save")
    public ResponseEntity<ApiResponse<SaveUnsavePostResponse>> saveUnsavePost(
            @PathVariable String postId,
            Authentication authentication
    ) {
        UUID userId = resolveUserId(authentication);
        SaveUnsavePostResult result = saveUnsavePostUseCase.execute(new SaveUnsavePostCommand(userId, postId));

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                saveUnsavePostUseCase.successMessage(result.saved()),
                toSaveUnsaveResponse(result)
        ));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<DeletePostResponse>> deletePost(
            @PathVariable String postId,
            Authentication authentication
    ) {
        AuthenticatedUser actor = resolveActor(authentication);
        DeletePostCommand command = new DeletePostCommand(
                actor != null ? actor.userId() : null,
                actor != null ? actor.roles() : List.of(),
                postId
        );
        DeletePostResult result = deletePostUseCase.execute(command);

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                deletePostUseCase.successMessage(),
                toDeleteResponse(result)
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

    private CreatePostCommand toCommand(CreatePostRequest request, UUID authorId) {
        List<CreatePostCommand.MediaItemCommand> media = request.media() != null
                ? request.media().stream()
                        .map(m -> new CreatePostCommand.MediaItemCommand(
                                m.url(), m.type(), m.width(), m.height()))
                        .toList()
                : List.of();
        List<CreatePostCommand.ProductTagCommand> productTags = request.productTags() != null
                ? request.productTags().stream()
                        .map(pt -> new CreatePostCommand.ProductTagCommand(pt.productId(), pt.price()))
                        .toList()
                : List.of();
        return new CreatePostCommand(
                authorId,
                request.caption(),
                media,
                productTags,
                request.visibility(),
                request.allowComments(),
                request.hashtags(),
                request.publish()
        );
    }

    private CreatePostResponse toResponse(CreatePostResult result) {
        List<CreatePostResponse.MediaItemResponse> media = result.media().stream()
                .map(m -> new CreatePostResponse.MediaItemResponse(m.url(), m.type(), m.width(), m.height()))
                .toList();
        List<CreatePostResponse.ProductTagResponse> productTags = result.productTags().stream()
                .map(com.twohands.social_service.delivery.http.post.mapper.ProductTagHttpMapper::toCreateResponse)
                .toList();
        return new CreatePostResponse(
                result.postId(),
                result.authorId(),
                result.caption(),
                media,
                productTags,
                result.status(),
                result.visibility(),
                result.allowComments(),
                result.hashtags(),
                result.createdAt(),
                result.updatedAt()
        );
    }

    private EditPostCommand toEditCommand(EditPostRequest request, UUID editorId, String postId) {
        var media = request.media()
                .map(items -> items.stream()
                        .map(m -> new EditPostCommand.MediaItemCommand(
                                m.url(), m.type(), m.width(), m.height()))
                        .toList());
        var productTags = request.productTags()
                .map(items -> items.stream()
                        .map(pt -> new EditPostCommand.ProductTagCommand(pt.productId(), pt.price()))
                        .toList());
        return new EditPostCommand(
                editorId,
                postId,
                request.caption(),
                media,
                productTags,
                request.visibility(),
                request.allowComments(),
                request.hashtags()
        );
    }

    private EditPostResponse toEditResponse(EditPostResult result) {
        List<EditPostResponse.MediaItemResponse> media = result.media().stream()
                .map(m -> new EditPostResponse.MediaItemResponse(m.url(), m.type(), m.width(), m.height()))
                .toList();
        List<EditPostResponse.ProductTagResponse> productTags = result.productTags().stream()
                .map(com.twohands.social_service.delivery.http.post.mapper.ProductTagHttpMapper::toEditResponse)
                .toList();
        return new EditPostResponse(
                result.postId(),
                result.authorId(),
                result.caption(),
                media,
                productTags,
                result.status(),
                result.visibility(),
                result.allowComments(),
                result.hashtags(),
                result.createdAt(),
                result.updatedAt()
        );
    }

    private DeletePostResponse toDeleteResponse(DeletePostResult result) {
        return new DeletePostResponse(
                result.postId(),
                result.status(),
                result.deletedAt(),
                result.updatedAt()
        );
    }

    private LikeUnlikePostResponse toLikeUnlikeResponse(LikeUnlikePostResult result) {
        return new LikeUnlikePostResponse(
                result.postId(),
                result.liked(),
                result.likeCount()
        );
    }

    private SaveUnsavePostResponse toSaveUnsaveResponse(SaveUnsavePostResult result) {
        return new SaveUnsavePostResponse(
                result.postId(),
                result.saved()
        );
    }

    private CommentPostCommand toCommentPostCommand(CommentPostRequest request, UUID authorId, String postId) {
        List<CommentMediaItem> media = request.media() != null
                ? request.media().stream()
                        .map(m -> new CommentMediaItem(m.url(), m.type()))
                        .toList()
                : List.of();
        return new CommentPostCommand(authorId, postId, request.contentText(), media);
    }

    private CommentPostResponse toCommentPostResponse(CommentPostResult result) {
        List<CommentPostResponse.MediaItemResponse> media = result.media().stream()
                .map(m -> new CommentPostResponse.MediaItemResponse(m.url(), m.type()))
                .toList();
        CommentPostResponse.AuthorResponse author = new CommentPostResponse.AuthorResponse(
                result.author().userId(),
                result.author().displayName(),
                result.author().avatarUrl()
        );
        return new CommentPostResponse(
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
}
