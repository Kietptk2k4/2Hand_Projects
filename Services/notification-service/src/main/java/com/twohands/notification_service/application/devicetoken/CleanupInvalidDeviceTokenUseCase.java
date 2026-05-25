package com.twohands.notification_service.application.devicetoken;

import com.twohands.notification_service.domain.devicetoken.DeactivateInvalidDeviceTokenOutcome;
import com.twohands.notification_service.domain.devicetoken.UserDeviceToken;
import com.twohands.notification_service.domain.devicetoken.UserDeviceTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class CleanupInvalidDeviceTokenUseCase {

    private static final Logger log = LoggerFactory.getLogger(CleanupInvalidDeviceTokenUseCase.class);

    private final UserDeviceTokenRepository userDeviceTokenRepository;
    private final DeactivateInvalidDeviceTokenUseCase deactivateInvalidDeviceTokenUseCase;
    private final int staleInactiveDays;

    public CleanupInvalidDeviceTokenUseCase(
            UserDeviceTokenRepository userDeviceTokenRepository,
            DeactivateInvalidDeviceTokenUseCase deactivateInvalidDeviceTokenUseCase,
            @Value("${notification.workers.cleanup-invalid-device-tokens.stale-inactive-days:90}") int staleInactiveDays
    ) {
        this.userDeviceTokenRepository = userDeviceTokenRepository;
        this.deactivateInvalidDeviceTokenUseCase = deactivateInvalidDeviceTokenUseCase;
        this.staleInactiveDays = staleInactiveDays;
    }

    public int execute(int batchSize) {
        if (batchSize <= 0 || staleInactiveDays <= 0) {
            return 0;
        }

        Instant staleBefore = Instant.now().minus(staleInactiveDays, ChronoUnit.DAYS);
        List<UserDeviceToken> candidates = userDeviceTokenRepository.findStaleActiveTokens(staleBefore, batchSize);
        if (candidates.isEmpty()) {
            return 0;
        }

        int deactivatedCount = 0;
        for (UserDeviceToken token : candidates) {
            DeactivateInvalidDeviceTokenResult result = deactivateInvalidDeviceTokenUseCase.execute(
                    new DeactivateInvalidDeviceTokenCommand(token.deviceToken())
            );
            if (result.outcome() == DeactivateInvalidDeviceTokenOutcome.DEACTIVATED) {
                deactivatedCount++;
            }
        }

        if (deactivatedCount > 0) {
            log.info(
                    "Cleanup invalid device tokens completed. deactivatedCount={} batchSize={} staleInactiveDays={}",
                    deactivatedCount,
                    batchSize,
                    staleInactiveDays
            );
        }
        return deactivatedCount;
    }
}
