package com.twohands.auth_service.domain.user;

import java.util.List;
import java.util.UUID;

public interface UserInvestigationSearchRepository {

	List<InvestigationUserSearchItem> searchByEmailFragment(String emailFragment, int limit);

	List<InvestigationUserSearchItem> findByUserId(UUID userId);
}
