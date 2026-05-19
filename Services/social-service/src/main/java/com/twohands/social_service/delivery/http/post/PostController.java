package com.twohands.social_service.delivery.http.post;

import com.twohands.social_service.application.post.createpost.CreatePostCommand;
import com.twohands.social_service.application.post.createpost.CreatePostResult;
import com.twohands.social_service.application.post.createpost.CreatePostUseCase;
import com.twohands.social_service.common.dto.ApiResponse;
import com.twohands.social_service.delivery.http.post.request.CreatePostRequest;
import com.twohands.social_service.delivery.http.post.response.CreatePostResponse;
import com.twohands.social_service.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/social/posts")
public class PostController {

    private final CreatePostUseCase createPostUseCase;

    public PostController(CreatePostUseCase createPostUseCase) {
        this.createPostUseCase = createPostUseCase;
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

    private UUID resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            return null;
        }
        return principal.userId();
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
}
