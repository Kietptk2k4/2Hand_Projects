package com.twohands.auth_service.application.admin.applyuserenforcement;

import com.twohands.auth_service.domain.enforcement.UserEnforcementActionType;

import java.time.Instant;
import java.util.UUID;

public record ApplyUserEnforcementCommand(
        UUID enforcementId,
        UUID userId,
        UserEnforcementActionType actionType,
        String reasonCode,
        String description,
        Instant expiresAt,
        UUID eventId,
        boolean failOnMissingUser,
        boolean reactivateUser
) {
    public static ApplyUserEnforcementCommand forSyncApply(
            UUID enforcementId,
            UUID userId,
            UserEnforcementActionType actionType,
            String reasonCode,
            String description,
            Instant expiresAt
    ) {
        return new ApplyUserEnforcementCommand(
                enforcementId,
                userId,
                actionType,
                reasonCode,
                description,
                expiresAt,
                null,
                true,
                false
        );
    }

    public static ApplyUserEnforcementCommand forSyncRevoke(
            UUID enforcementId,
            UUID userId,
            UserEnforcementActionType actionType,
            String reasonCode,
            String description,
            boolean reactivateUser
    ) {
        return new ApplyUserEnforcementCommand(
                enforcementId,
                userId,
                actionType,
                reasonCode,
                description,
                null,
                null,
                true,
                reactivateUser
        );
    }

    public static ApplyUserEnforcementCommand forEventApply(
            UUID eventId,
            UUID enforcementId,
            UUID userId,
            UserEnforcementActionType actionType,
            String reasonCode,
            String description,
            Instant expiresAt
    ) {
        return new ApplyUserEnforcementCommand(
                enforcementId,
                userId,
                actionType,
                reasonCode,
                description,
                expiresAt,
                eventId,
                false,
                true
        );
    }
}
