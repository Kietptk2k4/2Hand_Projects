package com.twohands.auth_service.application.useraccount.toggleprivacy;

import java.util.UUID;

public record TogglePrivacyCommand(
        UUID userId,
        boolean isPrivate
) {
}
