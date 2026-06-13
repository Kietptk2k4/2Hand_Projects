package com.twohands.auth_service.application.useraccount.coverupload;

import java.util.UUID;

public record CreateCoverUploadUrlCommand(UUID userId, String contentType, long fileSizeBytes) {
}
