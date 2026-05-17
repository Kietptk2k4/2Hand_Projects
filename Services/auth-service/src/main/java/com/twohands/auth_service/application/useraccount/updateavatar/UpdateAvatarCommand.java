package com.twohands.auth_service.application.useraccount.updateavatar;

import java.util.UUID;

public record UpdateAvatarCommand(
        UUID userId,
        String avatarUrl
) {
}
