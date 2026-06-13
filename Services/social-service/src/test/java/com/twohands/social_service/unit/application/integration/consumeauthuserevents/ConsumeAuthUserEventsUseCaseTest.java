package com.twohands.social_service.unit.application.integration.consumeauthuserevents;

import com.twohands.social_service.application.integration.common.UserAvatarUpdatedOutboxService;
import com.twohands.social_service.application.integration.consumeauthuserevents.AuthUserEventType;
import com.twohands.social_service.application.integration.consumeauthuserevents.ConsumeAuthUserEventCommand;
import com.twohands.social_service.application.integration.consumeauthuserevents.ConsumeAuthUserEventsUseCase;
import com.twohands.social_service.domain.follow.FollowRepository;
import com.twohands.social_service.domain.integration.ProcessedDomainEventRepository;
import com.twohands.social_service.domain.outbox.OutboxEvent;
import com.twohands.social_service.domain.outbox.OutboxEventRepository;
import com.twohands.social_service.domain.user.UserProjection;
import com.twohands.social_service.domain.user.UserProjectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConsumeAuthUserEventsUseCaseTest {

    private final ProcessedDomainEventRepository processedDomainEventRepository = mock(ProcessedDomainEventRepository.class);
    private final UserProjectionRepository userProjectionRepository = mock(UserProjectionRepository.class);
    private final OutboxEventRepository outboxEventRepository = mock(OutboxEventRepository.class);
    private final FollowRepository followRepository = mock(FollowRepository.class);
    private final UserAvatarUpdatedOutboxService userAvatarUpdatedOutboxService = mock(UserAvatarUpdatedOutboxService.class);

    private ConsumeAuthUserEventsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ConsumeAuthUserEventsUseCase(
                processedDomainEventRepository,
                userProjectionRepository,
                outboxEventRepository,
                followRepository,
                userAvatarUpdatedOutboxService
        );
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
                null,
                "https://cdn.2hands.vn/avatar.png",
                null,
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
                new UserProjection(userId.toString(), "ACTIVE", "User A", null, null, false)
        ));
        when(userProjectionRepository.upsert(any(UserProjection.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(new ConsumeAuthUserEventCommand(
                eventId,
                AuthUserEventType.USER_UPDATED,
                userId,
                null,
                null,
                null,
                "https://cdn.2hands.vn/new.png",
                null,
                true,
                null,
                null
        ));

        assertThat(result.appliedStatus()).isEqualTo("ACTIVE");
        verify(userProjectionRepository).upsert(any(UserProjection.class));
    }

    @Test
    void shouldUpdateCoverOnUserUpdated() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(processedDomainEventRepository.existsByEventId(eventId)).thenReturn(false);
        when(userProjectionRepository.findByUserId(userId)).thenReturn(Optional.of(
                new UserProjection(userId.toString(), "ACTIVE", "User A", null, null, false)
        ));
        when(userProjectionRepository.upsert(any(UserProjection.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = useCase.execute(new ConsumeAuthUserEventCommand(
                eventId,
                AuthUserEventType.USER_UPDATED,
                userId,
                null,
                null,
                null,
                null,
                "https://cdn.2hands.vn/cover.png",
                null,
                null,
                null
        ));

        assertThat(result.appliedStatus()).isEqualTo("ACTIVE");
        verify(userProjectionRepository).upsert(org.mockito.ArgumentMatchers.argThat(projection ->
                "https://cdn.2hands.vn/cover.png".equals(projection.coverUrl())
        ));
    }

    @Test
    void shouldMarkDeletedOnUserDeleted() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(processedDomainEventRepository.existsByEventId(eventId)).thenReturn(false);
        when(userProjectionRepository.findByUserId(userId)).thenReturn(Optional.of(
                new UserProjection(userId.toString(), "ACTIVE", "User A", null, null, false)
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
                new UserProjection(userId.toString(), "ACTIVE", "User A", null, null, false)
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
                new UserProjection(userId.toString(), "SUSPENDED", "User A", null, null, false)
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
    void shouldUseEmailLocalPartWhenDisplayNameMissing() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(processedDomainEventRepository.existsByEventId(eventId)).thenReturn(false);
        when(userProjectionRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userProjectionRepository.upsert(any(UserProjection.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(new ConsumeAuthUserEventCommand(
                eventId,
                AuthUserEventType.USER_CREATED,
                userId,
                "ACTIVE",
                null,
                "new.user@2hands.vn",
                null,
                null,
                false,
                null,
                null
        ));

        verify(userProjectionRepository).upsert(org.mockito.ArgumentMatchers.argThat(projection ->
                "new.user".equals(projection.displayName())
        ));
    }

    @Test
    void shouldReplacePlaceholderDisplayNameWithEmailLocalPart() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String placeholderName = "User " + userId.toString().substring(0, 8);

        when(processedDomainEventRepository.existsByEventId(eventId)).thenReturn(false);
        when(userProjectionRepository.findByUserId(userId)).thenReturn(Optional.of(
                new UserProjection(userId.toString(), "ACTIVE", placeholderName, null, null, false)
        ));
        when(userProjectionRepository.upsert(any(UserProjection.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(new ConsumeAuthUserEventCommand(
                eventId,
                AuthUserEventType.USER_UPDATED,
                userId,
                "ACTIVE",
                null,
                "real.name@2hands.vn",
                null,
                null,
                null,
                null,
                null
        ));

        verify(userProjectionRepository).upsert(org.mockito.ArgumentMatchers.argThat(projection ->
                "real.name".equals(projection.displayName())
        ));
    }

    @Test
    void shouldPublishAvatarUpdatedWhenAvatarChangesAndFollowersExist() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID followerId = UUID.randomUUID();
        OutboxEvent outboxEvent = mock(OutboxEvent.class);

        when(processedDomainEventRepository.existsByEventId(eventId)).thenReturn(false);
        when(userProjectionRepository.findByUserId(userId)).thenReturn(Optional.of(
                new UserProjection(userId.toString(), "ACTIVE", "User A", "https://cdn.2hands.vn/old.png", null, false)
        ));
        when(userProjectionRepository.upsert(any(UserProjection.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(followRepository.findAcceptedFollowerIds(userId)).thenReturn(List.of(followerId));
        when(userAvatarUpdatedOutboxService.build(
                org.mockito.ArgumentMatchers.eq(userId),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq("https://cdn.2hands.vn/new.png"),
                org.mockito.ArgumentMatchers.eq(List.of(followerId)),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(outboxEvent);

        useCase.execute(new ConsumeAuthUserEventCommand(
                eventId,
                AuthUserEventType.USER_UPDATED,
                userId,
                null,
                null,
                null,
                "https://cdn.2hands.vn/new.png",
                null,
                null,
                null,
                null
        ));

        verify(outboxEventRepository).save(outboxEvent);
    }

    @Test
    void shouldSkipAvatarUpdatedWhenAvatarUnchanged() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(processedDomainEventRepository.existsByEventId(eventId)).thenReturn(false);
        when(userProjectionRepository.findByUserId(userId)).thenReturn(Optional.of(
                new UserProjection(userId.toString(), "ACTIVE", "User A", "https://cdn.2hands.vn/same.png", null, false)
        ));
        when(userProjectionRepository.upsert(any(UserProjection.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(new ConsumeAuthUserEventCommand(
                eventId,
                AuthUserEventType.USER_UPDATED,
                userId,
                null,
                null,
                null,
                "https://cdn.2hands.vn/same.png",
                null,
                null,
                null,
                null
        ));

        verify(outboxEventRepository, never()).save(any());
        verify(followRepository, never()).findAcceptedFollowerIds(any());
    }

    @Test
    void shouldSkipDuplicateEvent() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(processedDomainEventRepository.existsByEventId(eventId)).thenReturn(true);
        when(userProjectionRepository.findByUserId(userId)).thenReturn(Optional.of(
                new UserProjection(userId.toString(), "SUSPENDED", "User A", null, null, false)
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
                null,
                null,
                null
        ));

        assertThat(result.skippedDuplicate()).isTrue();
        verify(userProjectionRepository, never()).upsert(any());
        verify(processedDomainEventRepository, never()).markProcessed(any(), any(), any());
    }
}
