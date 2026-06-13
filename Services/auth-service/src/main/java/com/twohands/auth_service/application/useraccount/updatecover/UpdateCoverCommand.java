package com.twohands.auth_service.application.useraccount.updatecover;

import java.util.UUID;

public record UpdateCoverCommand(UUID userId, String coverUrl) {
}
