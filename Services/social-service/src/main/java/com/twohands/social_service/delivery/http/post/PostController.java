package com.twohands.social_service.delivery.http.post;

import com.twohands.social_service.application.post.createpost.CreatePostCommand;
import com.twohands.social_service.application.post.createpost.CreatePostResult;
import com.twohands.social_service.application.post.createpost.CreatePostUseCase;
import com.twohands.social_service.application.post.deletepost.DeletePostCommand;
import com.twohands.social_service.application.post.deletepost.DeletePostResult;
import com.twohands.social_service.application.post.deletepost.DeletePostUseCase;
import com.twohands.social_service.application.post.editpost.EditPostCommand;
import com.twohands.social_service.application.post.editpost.EditPostResult;
import com.twohands.social_service.application.post.editpost.EditPostUseCase;
import com.twohands.social_service.common.dto.ApiResponse;
import com.twohands.social_service.delivery.http.post.request.CreatePostRequest;
import com.twohands.social_service.delivery.http.post.request.EditPostRequest;
import com.twohands.social_service.delivery.http.post.response.CreatePostResponse;
import com.twohands.social_service.delivery.http.post.response.DeletePostResponse;
import com.twohands.social_service.delivery.http.post.response.EditPostResponse;
import com.twohands.social_service.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/social/posts")
public class PostController {

    private final CreatePostUseCase createPostUseCase;
    private final EditPostUseCase editPostUseCase;
    private final DeletePostUseCase deletePostUseCase;

    public PostController(
            CreatePostUseCase createPostUseCase,
            EditPostUseCase editPostUseCase,
            DeletePostUseCase deletePostUseCase
    ) {
        this.createPostUseCase = createPostUseCase;
        this.editPostUseCase = editPostUseCase;
        this.deletePostUseCase = deletePostUseCase;
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
                        .map(m -> new CreatePostCommand.MediaItemCommand(m.url(), m.type()))
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
                .map(m -> new CreatePostResponse.MediaItemResponse(m.url(), m.type()))
                .toList();
        List<CreatePostResponse.ProductTagResponse> productTags = result.productTags().stream()
                .map(pt -> new CreatePostResponse.ProductTagResponse(pt.productId(), pt.price()))
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
                        .map(m -> new EditPostCommand.MediaItemCommand(m.url(), m.type()))
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
                .map(m -> new EditPostResponse.MediaItemResponse(m.url(), m.type()))
                .toList();
        List<EditPostResponse.ProductTagResponse> productTags = result.productTags().stream()
                .map(pt -> new EditPostResponse.ProductTagResponse(pt.productId(), pt.price()))
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
}
