package com.twohands.commerce_service.application.home.recommendproducts;

import com.twohands.commerce_service.domain.home.HomeInteractionRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.UUID;

@Service
public class TrackHomeProductClickUseCase {

    private final HomeInteractionRepository interactionRepository;
    private final Clock clock;

    public TrackHomeProductClickUseCase(HomeInteractionRepository interactionRepository, Clock clock) {
        this.interactionRepository = interactionRepository;
        this.clock = clock;
    }

    public void track(UUID userId, UUID productId, String requestId) {
        interactionRepository.saveClick(userId, productId, clock.instant(), requestId);
    }
}
