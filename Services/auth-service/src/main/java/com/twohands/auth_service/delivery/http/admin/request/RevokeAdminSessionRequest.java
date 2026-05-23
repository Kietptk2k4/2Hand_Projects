package com.twohands.auth_service.delivery.http.admin.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RevokeAdminSessionRequest(
        @JsonProperty("revoke_all_sessions")
        Boolean revokeAllSessions
) {
    public boolean revokeAllSessionsOrDefault() {
        return revokeAllSessions != null && revokeAllSessions;
    }
}
