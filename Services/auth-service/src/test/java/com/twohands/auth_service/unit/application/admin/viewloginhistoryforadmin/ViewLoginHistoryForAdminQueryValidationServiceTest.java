package com.twohands.auth_service.unit.application.admin.viewloginhistoryforadmin;

import com.twohands.auth_service.application.admin.viewloginhistoryforadmin.ViewLoginHistoryForAdminQueryValidationService;
import com.twohands.auth_service.domain.user.LoginLogQueryFilter;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ViewLoginHistoryForAdminQueryValidationServiceTest {

    private final ViewLoginHistoryForAdminQueryValidationService service =
            new ViewLoginHistoryForAdminQueryValidationService();

    @Test
    void shouldParseOptionalInstantFilters() {
        Instant from = Instant.parse("2026-05-01T00:00:00Z");
        Instant to = Instant.parse("2026-05-31T23:59:59Z");

        LoginLogQueryFilter filter = service.validateFilters(true, from.toString(), to.toString());

        assertThat(filter.success()).isTrue();
        assertThat(filter.from()).isEqualTo(from);
        assertThat(filter.to()).isEqualTo(to);
    }

    @Test
    void shouldRejectInvalidFromToRange() {
        assertThatThrownBy(() -> service.validateFilters(
                null,
                "2026-05-31T00:00:00Z",
                "2026-05-01T00:00:00Z"
        ))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
                    assertThat(appEx.getField()).isEqualTo("from");
                });
    }
}
