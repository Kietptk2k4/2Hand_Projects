package com.twohands.auth_service.application.admin.searchusersforinvestigation;

import com.twohands.auth_service.domain.user.InvestigationUserSearchItem;

import java.util.List;

public record SearchUsersForInvestigationResult(
		List<InvestigationUserSearchItem> users
) {
}
