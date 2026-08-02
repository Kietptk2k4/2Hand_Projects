package com.twohands.commerce_service.unit.application.home.recommendproducts;

import com.twohands.commerce_service.application.home.recommendproducts.TrackHomeProductClickUseCase;
import com.twohands.commerce_service.domain.home.HomeInteractionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TrackHomeProductClickUseCaseTest {

    @Mock
    private HomeInteractionRepository interactionRepository;

    @Test
    void savesClickWithRequestId() {
        Instant now = Instant.parse("2026-08-02T12:00:00Z");
        TrackHomeProductClickUseCase useCase = new TrackHomeProductClickUseCase(
                interactionRepository,
                Clock.fixed(now, ZoneOffset.UTC)
        );
        UUID userId = UUID.fromString("11111111-1111-4111-8111-111111111111");
        UUID productId = UUID.fromString("22222222-2222-4222-8222-222222222222");

        useCase.track(userId, productId, "req-123");

        verify(interactionRepository).saveClick(userId, productId, now, "req-123");
    }
}
