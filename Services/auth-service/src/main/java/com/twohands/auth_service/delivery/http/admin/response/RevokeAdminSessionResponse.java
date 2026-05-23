package com.twohands.auth_service.delivery.http.admin.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record RevokeAdminSessionResponse(
        @JsonProperty("target_admin_user_id")
        UUID targetAdminUserId,
        @JsonProperty("session_id")
        UUID sessionId,
        @JsonProperty("revoked_session_count")
        int revokedSessionCount,
        @JsonProperty("revoke_all_sessions")
        boolean revokeAllSessions
) {
}
