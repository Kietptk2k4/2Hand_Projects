package com.twohands.social_service.application.admin.viewrecommendationmodelstatus;

import com.twohands.social_service.domain.post.RankingModelRuntimeStatus;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import com.twohands.social_service.infrastructure.model.ModelLoader;
import com.twohands.social_service.security.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ViewRecommendationModelStatusUseCase {

    private static final Set<String> ALLOWED_ROLES = Set.of("ADMIN", "MODERATOR");
    private static final String SUCCESS_MESSAGE = "Lay trang thai ranking model thanh cong.";

    private final ModelLoader modelLoader;

    @Value("${social.recommendation.ranking.model:lightgbm}")
    private String rankingModelType;

    public ViewRecommendationModelStatusUseCase(ModelLoader modelLoader) {
        this.modelLoader = modelLoader;
    }

    public RankingModelRuntimeStatus execute(AuthenticatedUser actor) {
        ensureAdminOrModerator(actor);
        return modelLoader.resolveRuntimeStatus(rankingModelType);
    }

    public String successMessage() {
        return SUCCESS_MESSAGE;
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
