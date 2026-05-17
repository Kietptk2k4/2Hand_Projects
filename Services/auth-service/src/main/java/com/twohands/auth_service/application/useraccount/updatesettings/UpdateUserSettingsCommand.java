package com.twohands.auth_service.application.useraccount.updatesettings;

import java.util.UUID;

public record UpdateUserSettingsCommand(
        UUID userId,
        String appearanceMode
) {
}
