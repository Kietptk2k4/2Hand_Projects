package com.twohands.social_service.application.integration.consumeauthuserevents;

import java.util.UUID;

public record ConsumeAuthUserEventResult(
        UUID eventId,
        UUID userId,
        String appliedStatus,
        boolean skippedDuplicate
) {
}
