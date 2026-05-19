package com.twohands.social_service.security;

import java.util.List;
import java.util.UUID;

public record AuthenticatedUser(UUID userId, List<String> roles) {
}
