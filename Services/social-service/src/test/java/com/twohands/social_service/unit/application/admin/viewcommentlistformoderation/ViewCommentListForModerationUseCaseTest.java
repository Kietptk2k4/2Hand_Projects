package com.twohands.social_service.unit.application.admin.viewcommentlistformoderation;

import com.twohands.social_service.application.admin.viewcommentlistformoderation.ViewCommentListForModerationCommand;
import com.twohands.social_service.application.admin.viewcommentlistformoderation.ViewCommentListForModerationResult;
import com.twohands.social_service.application.admin.viewcommentlistformoderation.ViewCommentListForModerationUseCase;
import com.twohands.social_service.domain.admin.AdminCommentListCriteria;
import com.twohands.social_service.domain.admin.AdminCommentListItem;
import com.twohands.social_service.domain.admin.AdminCommentListRepository;
import com.twohands.social_service.domain.admin.AdminCommentListSortField;
import com.twohands.social_service.domain.post.PageResult;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import com.twohands.social_service.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

class ViewCommentListForModerationUseCaseTest {

    private final AdminCommentListRepository adminCommentListRepository =
            Mockito.mock(AdminCommentListRepository.class);
    private ViewCommentListForModerationUseCase useCase;
    private AuthenticatedUser actor;

    @BeforeEach
    void setup() {
        useCase = new ViewCommentListForModerationUseCase(adminCommentListRepository);
        actor = new AuthenticatedUser(UUID.randomUUID(), List.of("MODERATOR"), List.of("COMMENT_MODERATE"));
    }

    @Test
    void shouldReturnCommentListWithCreatedAtDefaultSort() {
        when(adminCommentListRepository.findPage(any())).thenReturn(
                new PageResult<>(
                        List.of(new AdminCommentListItem(
                                "674b100000000000000001",
                                "507f1f77bcf86cd799439011",
                                UUID.randomUUID().toString(),
                                "Hello",
                                "ACTIVE",
                                1L,
                                Instant.parse("2026-05-17T10:00:00Z"),
                                Instant.parse("2026-05-17T10:30:00Z")
                        )),
                        1,
                        20,
                        1L,
                        1,
                        false
                )
        );

        ViewCommentListForModerationResult result = useCase.execute(
                new ViewCommentListForModerationCommand(actor, null, null, null, null, 1, 20)
        );

        assertEquals(1, result.items().size());

        ArgumentCaptor<AdminCommentListCriteria> captor = ArgumentCaptor.forClass(AdminCommentListCriteria.class);
        verify(adminCommentListRepository).findPage(captor.capture());
        assertEquals(AdminCommentListSortField.CREATED_AT, captor.getValue().sortField());
    }

    @Test
    void shouldReturnForbiddenWhenActorHasNoAccess() {
        AuthenticatedUser regularUser = new AuthenticatedUser(UUID.randomUUID(), List.of("USER"), List.of());

        AppException ex = assertThrows(
                AppException.class,
                () -> useCase.execute(new ViewCommentListForModerationCommand(regularUser, null, null, null, null, 1, 20))
        );

        assertEquals(ErrorCode.FORBIDDEN, ex.getErrorCode());
    }
}
