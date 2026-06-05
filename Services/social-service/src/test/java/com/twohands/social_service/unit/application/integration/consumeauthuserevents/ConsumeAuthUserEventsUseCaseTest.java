package com.twohands.social_service.unit.application.integration.consumeauthuserevents;

import com.twohands.social_service.application.integration.consumeauthuserevents.AuthUserEventType;
import com.twohands.social_service.application.integration.consumeauthuserevents.ConsumeAuthUserEventCommand;
import com.twohands.social_service.application.integration.consumeauthuserevents.ConsumeAuthUserEventsUseCase;
import com.twohands.social_service.domain.integration.ProcessedDomainEventRepository;
import com.twohands.social_service.domain.user.UserProjection;
import com.twohands.social_service.domain.user.UserProjectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConsumeAuthUserEventsUseCaseTest {

    private final ProcessedDomainEventRepository processedDomainEventRepository = mock(ProcessedDomainEventRepository.class);
    private final UserProjectionRepository userProjectionRepository = mock(UserProjectionRepository.class);

    private ConsumeAuthUserEventsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ConsumeAuthUserEventsUseCase(processedDomainEventRepository, userProjectionRepository);
    }

    @Test
    void shouldCreateProjectionOnUserCreated() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(processedDomainEventRepository.existsByEventId(eventId)).thenReturn(false);
        when(userProjectionRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userProjectionRepository.upsert(any(UserProjection.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(new ConsumeAuthUserEventCommand(
                eventId,
                AuthUserEventType.USER_CREATED,
                userId,
                "ACTIVE",
                "User A",
                "https://cdn.2hands.vn/avatar.png",
                false,
                null,
                null
        ));

        assertThat(result.skippedDuplicate()).isFalse();
        assertThat(result.appliedStatus()).isEqualTo("ACTIVE");
        verify(processedDomainEventRepository).markProcessed(
                eventId,
                ConsumeAuthUserEventsUseCase.CONSUMER_NAME,
                "USER_CREATED"
        );
    }

    @Test
    void shouldUpdatePrivacyOnUserUpdated() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(processedDomainEventRepository.existsByEventId(eventId)).thenReturn(false);
        when(userProjectionRepository.findByUserId(userId)).thenReturn(Optional.of(
                new UserProjection(userId.toString(), "ACTIVE", "User A", null, false)
        ));
        when(userProjectionRepository.upsert(any(UserProjection.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(new ConsumeAuthUserEventCommand(
                eventId,
                AuthUserEventType.USER_UPDATED,
                userId,
                null,
                null,
                "https://cdn.2hands.vn/new.png",
                true,
                null,
                null
        ));

        assertThat(result.appliedStatus()).isEqualTo("ACTIVE");
        verify(userProjectionRepository).upsert(any(UserProjection.class));
    }

    @Test
    void shouldMarkDeletedOnUserDeleted() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(processedDomainEventRepository.existsByEventId(eventId)).thenReturn(false);
        when(userProjectionRepository.findByUserId(userId)).thenReturn(Optional.of(
                new UserProjection(userId.toString(), "ACTIVE", "User A", null, false)
        ));
        when(userProjectionRepository.upsert(any(UserProjection.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(new ConsumeAuthUserEventCommand(
                eventId,
                AuthUserEventType.USER_DELETED,
                userId,
                null,
                null,
                null,
                null,
                null,
                null
        ));

        assertThat(result.appliedStatus()).isEqualTo("DELETED");
    }

    @Test
    void shouldSuspendOnUserSuspended() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(processedDomainEventRepository.existsByEventId(eventId)).thenReturn(false);
        when(userProjectionRepository.findByUserId(userId)).thenReturn(Optional.of(
                new UserProjection(userId.toString(), "ACTIVE", "User A", null, false)
        ));
        when(userProjectionRepository.upsert(any(UserProjection.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(new ConsumeAuthUserEventCommand(
                eventId,
                AuthUserEventType.USER_SUSPENDED,
                userId,
                null,
                null,
                null,
                null,
                "SUSPEND",
                null
        ));

        assertThat(result.appliedStatus()).isEqualTo("SUSPENDED");
    }

    @Test
    void shouldActivateOnEnforcementRevoked() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(processedDomainEventRepository.existsByEventId(eventId)).thenReturn(false);
        when(userProjectionRepository.findByUserId(userId)).thenReturn(Optional.of(
                new UserProjection(userId.toString(), "SUSPENDED", "User A", null, false)
        ));
        when(userProjectionRepository.upsert(any(UserProjection.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(new ConsumeAuthUserEventCommand(
                eventId,
                AuthUserEventType.USER_ENFORCEMENT_REVOKED,
                userId,
                null,
                null,
                null,
                null,
                null,
                null
        ));

        assertThat(result.appliedStatus()).isEqualTo("ACTIVE");
        verify(processedDomainEventRepository).markProcessed(
                eventId,
                ConsumeAuthUserEventsUseCase.CONSUMER_NAME,
                "USER_ENFORCEMENT_REVOKED"
        );
    }

    @Test
    void shouldSkipDuplicateEvent() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(processedDomainEventRepository.existsByEventId(eventId)).thenReturn(true);
        when(userProjectionRepository.findByUserId(userId)).thenReturn(Optional.of(
                new UserProjection(userId.toString(), "SUSPENDED", "User A", null, false)
        ));

        var result = useCase.execute(new ConsumeAuthUserEventCommand(
                eventId,
                AuthUserEventType.USER_SUSPENDED,
                userId,
                null,
                null,
                null,
                null,
                null,
                null
        ));

        assertThat(result.skippedDuplicate()).isTrue();
        verify(userProjectionRepository, never()).upsert(any());
        verify(processedDomainEventRepository, never()).markProcessed(any(), any(), any());
    }
}
