package com.twohands.admin_service.delivery.http.session;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RevokeAdminSessionRequest(
		@JsonProperty("revoke_all_sessions")
		Boolean revokeAllSessions
) {
	public boolean revokeAllSessionsOrDefault() {
		return revokeAllSessions != null && revokeAllSessions;
	}
}
