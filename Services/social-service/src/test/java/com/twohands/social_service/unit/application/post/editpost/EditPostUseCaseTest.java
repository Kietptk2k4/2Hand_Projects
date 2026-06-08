package com.twohands.social_service.unit.application.post.editpost;

import com.twohands.social_service.application.post.common.PostMediaUrlValidator;
import com.twohands.social_service.application.post.common.ProductTagValidator;
import com.twohands.social_service.application.post.editpost.EditPostCommand;
import com.twohands.social_service.application.post.editpost.EditPostResult;
import com.twohands.social_service.application.post.editpost.EditPostUseCase;
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

class EditPostUseCaseTest {

    private final PostRepository postRepository = mock(PostRepository.class);
    private final UserProjectionRepository userProjectionRepository = mock(UserProjectionRepository.class);
    private final UserWriteGuard userWriteGuard = new UserWriteGuard(userProjectionRepository);
    private final PostMediaUrlValidator postMediaUrlValidator = mock(PostMediaUrlValidator.class);
    private final EditPostUseCase useCase = new EditPostUseCase(
            postRepository, userWriteGuard, new ProductTagValidator(), postMediaUrlValidator);

    private Post buildExistingPost(UUID authorId, String postId, PostStatus status) {
        return new Post(
                postId,
                authorId.toString(),
                "Original caption",
                List.of(new MediaItem("https://cdn/old.jpg", "IMAGE")),
                List.of(),
                status,
                PostVisibility.PUBLIC,
                5L,
                2L,
                List.of("old"),
                true,
                PostModerationStatus.NONE,
                null,
                null,
                Instant.parse("2026-05-19T00:00:00Z"),
                Instant.parse("2026-05-19T00:00:00Z"),
                null
        );
    }

    @Test
    void shouldUpdateOnlyProvidedFields() {
        UUID authorId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439011";
        Post existing = buildExistingPost(authorId, postId, PostStatus.ACTIVE);

        when(userProjectionRepository.findByUserId(authorId)).thenReturn(UserProjectionTestFixtures.activeOptional(authorId));
        when(postRepository.findById(postId)).thenReturn(Optional.of(existing));
        when(postRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EditPostCommand command = new EditPostCommand(
                authorId,
                postId,
                Optional.of("Updated caption"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );

        EditPostResult result = useCase.execute(command);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        Post saved = postCaptor.getValue();

        assertThat(saved.caption()).isEqualTo("Updated caption");
        assertThat(saved.media()).hasSize(1);
        assertThat(saved.media().getFirst().url()).isEqualTo("https://cdn/old.jpg");
        assertThat(saved.hashtags()).containsExactly("old");
        assertThat(saved.likeCount()).isEqualTo(5L);
        assertThat(saved.replyCount()).isEqualTo(2L);
        assertThat(saved.updatedAt()).isNotNull();

        assertThat(result.caption()).isEqualTo("Updated caption");
        assertThat(result.status()).isEqualTo("ACTIVE");
    }

    @Test
    void shouldReplaceMediaWhenProvided() {
        UUID authorId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439011";
        Post existing = buildExistingPost(authorId, postId, PostStatus.ACTIVE);

        when(userProjectionRepository.findByUserId(authorId)).thenReturn(UserProjectionTestFixtures.activeOptional(authorId));
        when(postRepository.findById(postId)).thenReturn(Optional.of(existing));
        when(postRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EditPostCommand command = new EditPostCommand(
                authorId,
                postId,
                Optional.empty(),
                Optional.of(List.of(new EditPostCommand.MediaItemCommand("https://cdn/new.mp4", "VIDEO", null, null))),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );

        useCase.execute(command);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        assertThat(postCaptor.getValue().media()).containsExactly(new MediaItem("https://cdn/new.mp4", "VIDEO"));
    }

    @Test
    void shouldThrowNotFoundWhenPostDoesNotExist() {
        UUID authorId = UUID.randomUUID();
        when(userProjectionRepository.findByUserId(authorId)).thenReturn(UserProjectionTestFixtures.activeOptional(authorId));
        when(postRepository.findById("missing")).thenReturn(Optional.empty());

        EditPostCommand command = new EditPostCommand(
                authorId, "missing", Optional.of("caption"),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    void shouldThrowNotFoundWhenPostIsDeleted() {
        UUID authorId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439011";
        when(userProjectionRepository.findByUserId(authorId)).thenReturn(UserProjectionTestFixtures.activeOptional(authorId));
        when(postRepository.findById(postId))
                .thenReturn(Optional.of(buildExistingPost(authorId, postId, PostStatus.DELETED)));

        EditPostCommand command = new EditPostCommand(
                authorId, postId, Optional.of("caption"),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    void shouldThrowForbiddenWhenEditorIsNotAuthor() {
        UUID authorId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439011";

        when(userProjectionRepository.findByUserId(otherUserId)).thenReturn(UserProjectionTestFixtures.activeOptional(otherUserId));
        when(postRepository.findById(postId))
                .thenReturn(Optional.of(buildExistingPost(authorId, postId, PostStatus.ACTIVE)));

        EditPostCommand command = new EditPostCommand(
                otherUserId, postId, Optional.of("caption"),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()
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

        EditPostCommand command = new EditPostCommand(
                authorId, "post-id", Optional.of("caption"),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_SUSPENDED));
    }

    @Test
    void shouldThrowValidationErrorWhenCaptionContainsScript() {
        UUID authorId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439011";

        when(userProjectionRepository.findByUserId(authorId)).thenReturn(UserProjectionTestFixtures.activeOptional(authorId));
        when(postRepository.findById(postId))
                .thenReturn(Optional.of(buildExistingPost(authorId, postId, PostStatus.ACTIVE)));

        EditPostCommand command = new EditPostCommand(
                authorId, postId, Optional.of("<script>alert(1)</script>"),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()
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
    void shouldReplaceProductTagsWhenProvided() {
        UUID authorId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439011";
        String productId = UUID.randomUUID().toString();
        Post existing = buildExistingPost(authorId, postId, PostStatus.ACTIVE);

        when(userProjectionRepository.findByUserId(authorId)).thenReturn(UserProjectionTestFixtures.activeOptional(authorId));
        when(postRepository.findById(postId)).thenReturn(Optional.of(existing));
        when(postRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EditPostCommand command = new EditPostCommand(
                authorId,
                postId,
                Optional.empty(),
                Optional.empty(),
                Optional.of(List.of(new EditPostCommand.ProductTagCommand(productId, new BigDecimal("99000")))),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );

        EditPostResult result = useCase.execute(command);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        assertThat(postCaptor.getValue().productTags())
                .containsExactly(new ProductTag(productId, new BigDecimal("99000")));
        assertThat(result.productTags()).hasSize(1);
        assertThat(result.productTags().getFirst().productId()).isEqualTo(productId);
    }

    @Test
    void shouldThrowValidationErrorWhenVisibilityIsInvalid() {
        UUID authorId = UUID.randomUUID();
        String postId = "507f1f77bcf86cd799439011";

        when(userProjectionRepository.findByUserId(authorId)).thenReturn(UserProjectionTestFixtures.activeOptional(authorId));
        when(postRepository.findById(postId))
                .thenReturn(Optional.of(buildExistingPost(authorId, postId, PostStatus.ACTIVE)));

        EditPostCommand command = new EditPostCommand(
                authorId, postId, Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.of("PRIVATE"),
                Optional.empty(), Optional.empty()
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
                    assertThat(appEx.getField()).isEqualTo("visibility");
                });
    }
}
