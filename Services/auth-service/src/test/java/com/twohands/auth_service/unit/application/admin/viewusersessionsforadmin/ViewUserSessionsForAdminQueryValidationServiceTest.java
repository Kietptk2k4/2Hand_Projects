package com.twohands.auth_service.unit.application.admin.viewusersessionsforadmin;

import com.twohands.auth_service.application.admin.viewusersessionsforadmin.ViewUserSessionsForAdminQueryValidationService;
import com.twohands.auth_service.domain.session.SessionStatus;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ViewUserSessionsForAdminQueryValidationServiceTest {

    private final ViewUserSessionsForAdminQueryValidationService service =
            new ViewUserSessionsForAdminQueryValidationService();

    @Test
    void shouldDefaultToActiveStatus() {
        var filter = service.validateStatus(null);
        assertThat(filter.status()).isEqualTo(SessionStatus.ACTIVE);
    }

    @Test
    void shouldAcceptAllStatusFilter() {
        var filter = service.validateStatus("ALL");
        assertThat(filter.status()).isNull();
    }

    @Test
    void shouldRejectInvalidLimit() {
        assertThatThrownBy(() -> service.validateLimit(51))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
                    assertThat(appEx.getField()).isEqualTo("limit");
                });
    }
}
