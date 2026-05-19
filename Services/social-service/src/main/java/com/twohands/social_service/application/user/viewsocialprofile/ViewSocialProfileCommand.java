package com.twohands.social_service.application.user.viewsocialprofile;

import java.util.UUID;

public record ViewSocialProfileCommand(
        UUID viewerId,
        UUID targetUserId
) {
}
