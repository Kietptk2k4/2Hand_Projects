package com.twohands.social_service.unit.application.post.createpost;

import com.twohands.social_service.application.post.common.PostMediaUrlValidator;
import com.twohands.social_service.application.post.common.ProductTagValidator;
import com.twohands.social_service.application.post.createpost.CreatePostCommand;
import com.twohands.social_service.application.post.createpost.CreatePostResult;
import com.twohands.social_service.application.post.createpost.CreatePostUseCase;
import com.twohands.social_service.domain.post.ProductTag;
import com.twohands.social_service.domain.post.MediaItem;
import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostModerationStatus;
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.domain.post.PostStatus;
import com.twohands.social_service.domain.post.PostVisibility;
import com.twohands.social_service.domain.user.UserProjection;
import com.twohands.social_service.application.user.common.UserWriteGuard;
import com.twohands.social_service.domain.user.UserProjectionRepository;
import com.twohands.social_service.testsupport.UserProjectionTestFixtures;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreatePostUseCaseTest {

    private final PostRepository postRepository = mock(PostRepository.class);
    private final UserProjectionRepository userProjectionRepository = mock(UserProjectionRepository.class);
    private final UserWriteGuard userWriteGuard = new UserWriteGuard(userProjectionRepository);
    private final PostMediaUrlValidator postMediaUrlValidator = mock(PostMediaUrlValidator.class);
    private final CreatePostUseCase useCase = new CreatePostUseCase(
            postRepository, userWriteGuard, new ProductTagValidator(), postMediaUrlValidator);

    private Post buildSavedPost(UUID authorId, String postId, PostStatus status, PostVisibility visibility) {
        return new Post(
                postId,
                authorId.toString(),
                "Hello world",
                List.of(new MediaItem("https://cdn/1.jpg", "IMAGE")),
                List.of(),
                status,
                visibility,
                0L,
                0L,
                List.of("spring"),
                true,
                PostModerationStatus.NONE,
                null,
                null,
                Instant.now(),
                Instant.now(),
                null
        );
    }

    @Test
    void shouldCreateActivePostWhenPublishIsTrue() {
        UUID authorId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(authorId)).thenReturn(UserProjectionTestFixtures.activeOptional(authorId));
        when(postRepository.save(any())).thenAnswer(inv -> {
            Post p = inv.getArgument(0);
            return buildSavedPost(authorId, "507f1f77bcf86cd799439011", p.status(), p.visibility());
        });

        CreatePostCommand command = new CreatePostCommand(
                authorId,
                "Hello world",
                List.of(new CreatePostCommand.MediaItemCommand("https://cdn/1.jpg", "IMAGE", null, null)),
                List.of(),
                "PUBLIC",
                true,
                List.of("spring"),
                true
        );

        CreatePostResult result = useCase.execute(command);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        assertThat(postCaptor.getValue().status()).isEqualTo(PostStatus.ACTIVE);
        assertThat(postCaptor.getValue().visibility()).isEqualTo(PostVisibility.PUBLIC);
        assertThat(postCaptor.getValue().authorId()).isEqualTo(authorId.toString());

        assertThat(result.postId()).isEqualTo("507f1f77bcf86cd799439011");
        assertThat(result.status()).isEqualTo("ACTIVE");
        assertThat(result.visibility()).isEqualTo("PUBLIC");
    }

    @Test
    void shouldCreateDraftPostWhenPublishIsFalse() {
        UUID authorId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(authorId)).thenReturn(UserProjectionTestFixtures.activeOptional(authorId));
        when(postRepository.save(any())).thenAnswer(inv -> {
            Post p = inv.getArgument(0);
            return buildSavedPost(authorId, "draft-id", p.status(), p.visibility());
        });

        CreatePostCommand command = new CreatePostCommand(
                authorId, "Draft caption", List.of(), List.of(), "FOLLOWERS", true, List.of(), false
        );

        CreatePostResult result = useCase.execute(command);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        assertThat(postCaptor.getValue().status()).isEqualTo(PostStatus.DRAFT);
        assertThat(result.status()).isEqualTo("DRAFT");
    }

    @Test
    void shouldThrowUnauthorizedWhenAuthorIdIsNull() {
        CreatePostCommand command = new CreatePostCommand(
                null, "caption", List.of(), List.of(), "PUBLIC", true, List.of(), true
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
                });
    }

    @Test
    void shouldRejectWriteWhenProjectionMissing() {
        UUID authorId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(authorId)).thenReturn(Optional.empty());

        CreatePostCommand command = new CreatePostCommand(
                authorId, "caption", List.of(), List.of(), "PUBLIC", true, List.of(), true
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
    }

    @Test
    void shouldThrowForbiddenWhenUserIsSuspended() {
        UUID authorId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(authorId))
                .thenReturn(Optional.of(new UserProjection(authorId.toString(), "SUSPENDED", "User", null, false)));

        CreatePostCommand command = new CreatePostCommand(
                authorId, "caption", List.of(), List.of(), "PUBLIC", true, List.of(), true
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_SUSPENDED);
                });
    }

    @Test
    void shouldThrowValidationErrorWhenVisibilityIsInvalid() {
        UUID authorId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(authorId)).thenReturn(UserProjectionTestFixtures.activeOptional(authorId));

        CreatePostCommand command = new CreatePostCommand(
                authorId, "caption", List.of(), List.of(), "PRIVATE", true, List.of(), true
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
                    assertThat(appEx.getField()).isEqualTo("visibility");
                });
    }

    @Test
    void shouldThrowValidationErrorWhenCaptionExceedsLimit() {
        UUID authorId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(authorId)).thenReturn(UserProjectionTestFixtures.activeOptional(authorId));

        String longCaption = "a".repeat(2001);
        CreatePostCommand command = new CreatePostCommand(
                authorId, longCaption, List.of(), List.of(), "PUBLIC", true, List.of(), true
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
                    assertThat(appEx.getField()).isEqualTo("caption");
                });
    }

    @Test
    void shouldThrowValidationErrorWhenMediaTypeIsInvalid() {
        UUID authorId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(authorId)).thenReturn(UserProjectionTestFixtures.activeOptional(authorId));

        CreatePostCommand command = new CreatePostCommand(
                authorId, "caption",
                List.of(new CreatePostCommand.MediaItemCommand("https://cdn/1.jpg", "GIF", null, null)),
                List.of(), "PUBLIC", true, List.of(), true
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
                    assertThat(appEx.getField()).isEqualTo("media[].type");
                });
    }

    @Test
    void shouldPersistProductTagsWhenProvided() {
        UUID authorId = UUID.randomUUID();
        String productId = UUID.randomUUID().toString();
        when(userProjectionRepository.findByUserId(authorId)).thenReturn(UserProjectionTestFixtures.activeOptional(authorId));
        when(postRepository.save(any())).thenAnswer(inv -> {
            Post p = inv.getArgument(0);
            return new Post(
                    "507f1f77bcf86cd799439011",
                    p.authorId(),
                    p.caption(),
                    p.media(),
                    p.productTags(),
                    p.status(),
                    p.visibility(),
                    0L,
                    0L,
                    p.hashtags(),
                    p.allowComments(),
                    p.moderationStatusOrDefault(),
                    p.moderationReason(),
                    p.lastModerationLogId(),
                    p.createdAt(),
                    p.updatedAt(),
                    null
            );
        });

        CreatePostCommand command = new CreatePostCommand(
                authorId,
                "Selling item",
                List.of(),
                List.of(new CreatePostCommand.ProductTagCommand(productId, new BigDecimal("150000"))),
                "PUBLIC",
                true,
                List.of(),
                true
        );

        CreatePostResult result = useCase.execute(command);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        assertThat(postCaptor.getValue().productTags())
                .containsExactly(new ProductTag(productId, new BigDecimal("150000")));
        assertThat(result.productTags()).hasSize(1);
        assertThat(result.productTags().getFirst().productId()).isEqualTo(productId);
        assertThat(result.productTags().getFirst().price()).isEqualByComparingTo("150000");
    }

    @Test
    void shouldThrowValidationErrorWhenProductIdIsNotUuid() {
        UUID authorId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(authorId)).thenReturn(UserProjectionTestFixtures.activeOptional(authorId));

        CreatePostCommand command = new CreatePostCommand(
                authorId, "caption", List.of(),
                List.of(new CreatePostCommand.ProductTagCommand("not-a-uuid", null)),
                "PUBLIC", true, List.of(), true
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
                    assertThat(appEx.getField()).isEqualTo("productTags[].product_id");
                });
    }

    @Test
    void shouldThrowValidationErrorWhenTooManyHashtags() {
        UUID authorId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(authorId)).thenReturn(UserProjectionTestFixtures.activeOptional(authorId));

        List<String> tooManyTags = java.util.stream.IntStream.range(0, 31)
                .mapToObj(i -> "tag" + i)
                .toList();

        CreatePostCommand command = new CreatePostCommand(
                authorId, "caption", List.of(), List.of(), "PUBLIC", true, tooManyTags, true
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
                    assertThat(appEx.getField()).isEqualTo("hashtags");
                });
    }
}
