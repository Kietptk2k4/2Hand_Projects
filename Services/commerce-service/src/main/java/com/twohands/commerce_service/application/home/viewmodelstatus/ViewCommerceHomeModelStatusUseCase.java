package com.twohands.commerce_service.application.home.viewmodelstatus;

import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.infrastructure.model.HomeModelLoader;
import com.twohands.commerce_service.security.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ViewCommerceHomeModelStatusUseCase {

    private static final Set<String> ALLOWED_ROLES = Set.of("ADMIN", "MODERATOR");
    private static final String SUCCESS_MESSAGE = "Lay trang thai Commerce Home ranking model thanh cong.";

    private final HomeModelLoader homeModelLoader;

    @Value("${commerce.home.recommendation.ranking-model:lightgbm}")
    private String configuredRankingModel;

    public ViewCommerceHomeModelStatusUseCase(HomeModelLoader homeModelLoader) {
        this.homeModelLoader = homeModelLoader;
    }

    @Transactional(readOnly = true)
    public CommerceHomeModelStatus execute(AuthenticatedUser actor) {
        ensureAdminOrModerator(actor);
        HomeModelLoader.HomeModelRuntime runtime = homeModelLoader.resolveRuntime();
        return new CommerceHomeModelStatus(
                runtime.rankingMode().name(),
                runtime.modelVersion(),
                runtime.modelName(),
                runtime.fallbackReason(),
                normalizeConfiguredRankingModel(configuredRankingModel)
        );
    }

    public String successMessage() {
        return SUCCESS_MESSAGE;
    }

    private static String normalizeConfiguredRankingModel(String configuredRankingModel) {
        if (configuredRankingModel == null || configuredRankingModel.isBlank()) {
            return "lightgbm";
        }
        return configuredRankingModel.trim();
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

    public record CommerceHomeModelStatus(
            String mode,
            Integer modelVersion,
            String modelName,
            String reason,
            String configuredRankingModel
    ) {
    }
}
