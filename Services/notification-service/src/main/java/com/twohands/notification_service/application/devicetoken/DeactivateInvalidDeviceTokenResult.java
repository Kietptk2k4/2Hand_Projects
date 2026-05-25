package com.twohands.notification_service.application.devicetoken;

import com.twohands.notification_service.domain.devicetoken.DeactivateInvalidDeviceTokenOutcome;

public record DeactivateInvalidDeviceTokenResult(
        DeactivateInvalidDeviceTokenOutcome outcome
) {

    public static DeactivateInvalidDeviceTokenResult deactivated() {
        return new DeactivateInvalidDeviceTokenResult(DeactivateInvalidDeviceTokenOutcome.DEACTIVATED);
    }

    public static DeactivateInvalidDeviceTokenResult alreadyInactive() {
        return new DeactivateInvalidDeviceTokenResult(DeactivateInvalidDeviceTokenOutcome.ALREADY_INACTIVE);
    }

    public static DeactivateInvalidDeviceTokenResult notFound() {
        return new DeactivateInvalidDeviceTokenResult(DeactivateInvalidDeviceTokenOutcome.NOT_FOUND);
    }
}
