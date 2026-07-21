package com.twohands.social_service.unit.application.admin.viewpostdetailformoderation;

import com.twohands.social_service.application.admin.common.AdminModerationAuthorResolver;
import com.twohands.social_service.application.admin.viewpostdetailformoderation.ViewPostDetailForModerationCommand;
import com.twohands.social_service.application.admin.viewpostdetailformoderation.ViewPostDetailForModerationResult;
import com.twohands.social_service.application.admin.viewpostdetailformoderation.ViewPostDetailForModerationUseCase;
import com.twohands.social_service.application.post.common.PostIdValidator;
import com.twohands.social_service.domain.post.MediaItem;
import com.twohands.social_service.domain.post.Post;
import com.twohands.social_service.domain.post.PostModerationStatus;
import com.twohands.social_service.domain.post.PostRepository;
import com.twohands.social_service.domain.post.PostStatus;
import com.twohands.social_service.domain.post.PostVisibility;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import com.twohands.social_service.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ViewPostDetailForModerationUseCaseTest {

    private final PostRepository postRepository = Mockito.mock(PostRepository.class);
    private final PostIdValidator postIdValidator = Mockito.mock(PostIdValidator.class);
    private final AdminModerationAuthorResolver authorResolver = Mockito.mock(AdminModerationAuthorResolver.class);
    private ViewPostDetailForModerationUseCase useCase;
    private AuthenticatedUser actor;

    @BeforeEach
    void setup() {
        useCase = new ViewPostDetailForModerationUseCase(postRepository, postIdValidator, authorResolver);
        actor = new AuthenticatedUser(UUID.randomUUID(), List.of("ADMIN"), List.of("POST_MODERATE"));
    }

    @Test
    void shouldReturnPostDetailForModerator() {
        String postId = "507f1f77bcf86cd799439011";
        String authorId = UUID.randomUUID().toString();
        Post post = new Post(
                postId,
                authorId,
                "Full caption",
                List.of(new MediaItem("https://cdn.example/post.jpg", "IMAGE", 800, 600)),
                List.of(),
                PostStatus.ACTIVE,
                PostVisibility.PUBLIC,
                12L,
                3L,
                List.of("tag"),
                true,
                PostModerationStatus.HIDDEN,
                "Policy violation",
                "log-1",
                Instant.parse("2026-05-17T10:00:00Z"),
                Instant.parse("2026-05-17T11:00:00Z"),
                null
        );

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(authorResolver.resolveAuthor(authorId)).thenReturn(
                new AdminModerationAuthorResolver.AuthorSummary(authorId, "Author Name", "https://avatar")
        );

        ViewPostDetailForModerationResult result = useCase.execute(
                new ViewPostDetailForModerationCommand(actor, postId)
        );

        assertEquals(postId, result.id());
        assertEquals("Full caption", result.caption());
        assertEquals("HIDDEN", result.moderationStatus());
        assertEquals("Author Name", result.author().displayName());
        assertEquals(1, result.mediaCount());
        verify(postIdValidator).validate(postId);
    }

    @Test
    void shouldReturnNotFoundWhenPostMissing() {
        String postId = "507f1f77bcf86cd799439011";
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        AppException ex = assertThrows(
                AppException.class,
                () -> useCase.execute(new ViewPostDetailForModerationCommand(actor, postId))
        );

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void shouldReturnForbiddenWhenActorHasNoAccess() {
        AuthenticatedUser regularUser = new AuthenticatedUser(UUID.randomUUID(), List.of("USER"), List.of());

        AppException ex = assertThrows(
                AppException.class,
                () -> useCase.execute(new ViewPostDetailForModerationCommand(regularUser, "507f1f77bcf86cd799439011"))
        );

        assertEquals(ErrorCode.FORBIDDEN, ex.getErrorCode());
        verify(postIdValidator, Mockito.never()).validate(any());
    }
}
