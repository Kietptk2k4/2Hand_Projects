package com.twohands.auth_service.application.useraccount.softdelete;

import java.util.UUID;

public record SoftDeleteAccountCommand(
        UUID userId,
        String password
) {
}
