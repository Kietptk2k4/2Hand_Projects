package com.twohands.commerce_service.application.home.viewmodelartifacts;

import com.twohands.commerce_service.domain.home.HomeModelArtifactRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.security.AuthenticatedUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ViewCommerceHomeModelArtifactsUseCase {

    private static final Set<String> ALLOWED_ROLES = Set.of("ADMIN", "MODERATOR");
    private static final String DEFAULT_MODEL_NAME = "commerce_home_ranker";
    private static final String SUCCESS_MESSAGE = "Lay danh sach Commerce Home model artifact thanh cong.";

    private final HomeModelArtifactRepository homeModelArtifactRepository;

    public ViewCommerceHomeModelArtifactsUseCase(HomeModelArtifactRepository homeModelArtifactRepository) {
        this.homeModelArtifactRepository = homeModelArtifactRepository;
    }

    @Transactional(readOnly = true)
    public List<HomeModelArtifactRepository.ModelArtifactListItem> execute(
            AuthenticatedUser actor,
            String modelName
    ) {
        ensureAdminOrModerator(actor);
        return homeModelArtifactRepository.listByModelName(normalizeModelName(modelName));
    }

    public String successMessage() {
        return SUCCESS_MESSAGE;
    }

    private static String normalizeModelName(String modelName) {
        if (modelName == null || modelName.isBlank()) {
            return DEFAULT_MODEL_NAME;
        }
        return modelName.trim();
    }

    private static void ensureAdminOrModerator(AuthenticatedUser actor) {
        if (actor == null || actor.userId() == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.defaultMessage());
        }
        List<String> roles = actor.roles();
        if (roles == null || roles.isEmpty()) {
            throw new AppException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.defaultMessage());
        }
        boolean allowed = roles.stream()
                .map(role -> role == null ? "" : role.trim().toUpperCase(Locale.ROOT))
                .anyMatch(ALLOWED_ROLES::contains);
        if (!allowed) {
            throw new AppException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.defaultMessage());
        }
    }
}
