package com.twohands.auth_service.application.admin.searchusersforinvestigation;

import com.twohands.auth_service.domain.rbac.AuthorizationDomainService;
import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
import com.twohands.auth_service.domain.user.InvestigationUserSearchItem;
import com.twohands.auth_service.domain.user.UserInvestigationSearchRepository;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class SearchUsersForInvestigationUseCase {

	private static final String USER_INVESTIGATION_READ_PERMISSION = "USER_INVESTIGATION_READ";
	private static final String SUCCESS_MESSAGE = "User investigation search completed successfully";
	private static final int DEFAULT_LIMIT = 20;
	private static final int MAX_LIMIT = 50;
	private static final int MIN_EMAIL_QUERY_LENGTH = 2;

	private final UserInvestigationSearchRepository userInvestigationSearchRepository;
	private final PermissionQueryRepository permissionQueryRepository;
	private final AuthorizationDomainService authorizationDomainService;

	public SearchUsersForInvestigationUseCase(
			UserInvestigationSearchRepository userInvestigationSearchRepository,
			PermissionQueryRepository permissionQueryRepository
	) {
		this.userInvestigationSearchRepository = userInvestigationSearchRepository;
		this.permissionQueryRepository = permissionQueryRepository;
		this.authorizationDomainService = new AuthorizationDomainService();
	}

	@Transactional(readOnly = true)
	public SearchUsersForInvestigationResult execute(SearchUsersForInvestigationCommand command) {
		UUID actorAdminId = requireActor(command.actorAdminId());
		ensureActorCanSearch(actorAdminId);

		String normalizedQuery = normalizeQuery(command.query());
		if (normalizedQuery.isEmpty()) {
			return new SearchUsersForInvestigationResult(List.of());
		}

		int limit = normalizeLimit(command.limit());
		List<InvestigationUserSearchItem> users = resolveUsers(normalizedQuery, limit);
		return new SearchUsersForInvestigationResult(users);
	}

	public String successMessage() {
		return SUCCESS_MESSAGE;
	}

	private List<InvestigationUserSearchItem> resolveUsers(String normalizedQuery, int limit) {
		UUID userId = tryParseUuid(normalizedQuery);
		if (userId != null) {
			return userInvestigationSearchRepository.findByUserId(userId);
		}
		if (normalizedQuery.length() < MIN_EMAIL_QUERY_LENGTH) {
			return List.of();
		}
		return userInvestigationSearchRepository.searchByEmailFragment(normalizedQuery, limit);
	}

	private UUID tryParseUuid(String value) {
		try {
			return UUID.fromString(value);
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}

	private String normalizeQuery(String rawQuery) {
		if (rawQuery == null) {
			return "";
		}
		return rawQuery.trim().toLowerCase(Locale.ROOT);
	}

	private int normalizeLimit(int limit) {
		if (limit <= 0) {
			return DEFAULT_LIMIT;
		}
		return Math.min(limit, MAX_LIMIT);
	}

	private UUID requireActor(UUID actorAdminId) {
		if (actorAdminId == null) {
			throw new AppException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.defaultMessage());
		}
		return actorAdminId;
	}

	private void ensureActorCanSearch(UUID actorAdminId) {
		Set<String> permissions = permissionQueryRepository.findPermissionCodesByUserId(actorAdminId);
		if (!authorizationDomainService.hasPermission(permissions, USER_INVESTIGATION_READ_PERMISSION)) {
			throw new AppException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.defaultMessage());
		}
	}
}
