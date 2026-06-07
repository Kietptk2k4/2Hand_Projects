package com.twohands.social_service.application.user.viewsuggestedusers;

import java.util.UUID;

public record ViewSuggestedUsersCommand(UUID viewerId, int page, int size) {
}